package pl.wachala.optimizer;

import pl.wachala.models.DiscountOption;
import pl.wachala.models.Order;
import pl.wachala.models.PaymentMethod;
import pl.wachala.models.PromotionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class PaymentMethodAssigner {
    public static final String LOYALTY_POINTS_PAYMENT_METHOD_ID = "PUNKTY";
    private static final BigDecimal PARTIAL_LOYALTY_POINTS_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(10);

    public Map<String, DiscountOption> assign(List<Order> orders, List<PaymentMethod> paymentMethods) {

        String defaultPaymentMethod = getDefaultPaymentMethod(paymentMethods);

        //payment method id -> payment method mapping
        HashMap<String, PaymentMethod> methodsMap = new HashMap<>();
        for (PaymentMethod method : paymentMethods) {
            methodsMap.put(method.getId(), method);
        }

        //calculate possible discounts for each order
        List<DiscountOption> possibleDiscounts = getPossibleDiscounts(orders, methodsMap, defaultPaymentMethod);

        //sort possible discounts by promotion limits used
        possibleDiscounts.sort(Comparator.comparing(DiscountOption::getPromoLimitUsed).reversed());

        //assign the most beneficial discount to order within global limit
        return getAssignmentGreedy(orders, paymentMethods, possibleDiscounts, defaultPaymentMethod);
    }

    //select default payment method that'd be used in case there's no promotion assigned, and we're unable to fully pay with loyalty points
    String getDefaultPaymentMethod(List<PaymentMethod> paymentMethods) {
        return paymentMethods.stream()
                .map(PaymentMethod::getId)
                .filter(id -> !Objects.equals(id, LOYALTY_POINTS_PAYMENT_METHOD_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to assign payment method to order since no default method available"));
    }

    Map<String, DiscountOption> getAssignmentGreedy(List<Order> orders, List<PaymentMethod> paymentMethods, List<DiscountOption> possibleDiscounts, String defaultPaymentMethod) {
        Map<String, DiscountOption> coveredOrders = new HashMap<>();
        Map<String, BigDecimal> availableLimit = new HashMap<>();

        for (PaymentMethod paymentMethod : paymentMethods) {
            availableLimit.put(paymentMethod.getId(), paymentMethod.getLimit());
        }

        // apply card promotions first
        for (DiscountOption discountOption : possibleDiscounts) {
            //we already have this order covered - no further processing needed
            if (coveredOrders.containsKey(discountOption.getOrderId())) {
                continue;
            }

            //update limits
            BigDecimal limitLeft = availableLimit.get(discountOption.getPromoMethodId());

            //If limit left is greater or equal to what we want to apply

            if (limitLeft.compareTo(discountOption.getPromoLimitUsed()) >= 0) {
                coveredOrders.put(discountOption.getOrderId(), discountOption);
                availableLimit.put(discountOption.getPromoMethodId(), limitLeft.subtract(discountOption.getPromoLimitUsed()));
            }
        }

        //then try to apply loyalty points (below 10%) if any left (full payment with loyalty points should not be possible here)
        if (availableLimit.containsKey(LOYALTY_POINTS_PAYMENT_METHOD_ID)) {
            for (Order order : orders) {
                //if order is already covered (has assigned promo) - skip it
                if (coveredOrders.containsKey(order.getId())) {
                    continue;
                }

                //get loyalty points left
                BigDecimal availableLoyaltyPoints = availableLimit.get(LOYALTY_POINTS_PAYMENT_METHOD_ID);

                //if available limits greater than zero
                if (availableLoyaltyPoints.compareTo(BigDecimal.ZERO) >= 1) {
                    //get minimum of (order value, available points)
                    BigDecimal loyaltyPointsToUse = availableLoyaltyPoints.min(order.getValue());
                    availableLimit.put(LOYALTY_POINTS_PAYMENT_METHOD_ID, availableLoyaltyPoints.subtract(loyaltyPointsToUse));
                    BigDecimal valueAfterDiscount = order.getValue().subtract(loyaltyPointsToUse);

                    coveredOrders.put(order.getId(), DiscountOption.builder()
                            .orderId(order.getId())
                            .paymentMethodId(defaultPaymentMethod)
                            .promoMethodId(LOYALTY_POINTS_PAYMENT_METHOD_ID)
                            .discount(loyaltyPointsToUse)
                            .promoLimitUsed(loyaltyPointsToUse)
                            .valueAfterDiscount(valueAfterDiscount)
                            .build());
                } else {
                    //No promotion applied - we pay with default card
                    coveredOrders.put(order.getId(), DiscountOption.builder()
                            .discount(BigDecimal.ZERO)
                            .promoLimitUsed(BigDecimal.ZERO)
                            .paymentMethodId(defaultPaymentMethod)
                            .promoMethodId(defaultPaymentMethod)
                            .valueAfterDiscount(order.getValue())
                            .build());
                }
            }
        }

        return coveredOrders;
    }

    List<DiscountOption> getPossibleDiscounts(List<Order> orders, HashMap<String, PaymentMethod> methodsMap, String defaultPaymentMethod) {
        List<DiscountOption> possibleDiscounts = new LinkedList<>();
        for (Order order : orders) {
            List<DiscountOption> discountOptions = calculateDiscountPerMethod(order, methodsMap, defaultPaymentMethod);
            possibleDiscounts.addAll(discountOptions);
        }
        return possibleDiscounts;
    }

    List<DiscountOption> calculateDiscountPerMethod(Order order, HashMap<String, PaymentMethod> methodsMap, String defaultPaymentMethod) {
        List<DiscountOption> result = new LinkedList<>();

        //CARD promotions calculated first
        for (String promo : order.getPromotions()) {
            PaymentMethod paymentMethod = methodsMap.get(promo);
            if (paymentMethod != null) {
                //discount percentage as BigDecimal could be pre-processed
                result.add(calculateCardPromo(order, paymentMethod));
            }
        }

        PaymentMethod loyaltyPointsMethod = methodsMap.get(LOYALTY_POINTS_PAYMENT_METHOD_ID);
        if (loyaltyPointsMethod != null) {
            // discount based on loyalty points >= 10%
            result.add(calculatePartialLoyaltyPointsPromo(order, defaultPaymentMethod));

            // discount based on loyalty points == 100%
            result.add(calculateFullLoyaltyPointsPromo(order, loyaltyPointsMethod));
        }

        return result;
    }

    DiscountOption calculateCardPromo(Order order, PaymentMethod paymentMethod) {
        BigDecimal totalDiscount = calculatePossibleDiscount(order.getValue(), paymentMethod);
        return DiscountOption.builder()
                .orderId(order.getId())
                .discount(totalDiscount)
                .promoLimitUsed(totalDiscount)
                .promoType(PromotionType.CARD)
                .valueAfterDiscount(order.getValue().subtract(totalDiscount))
                .paymentMethodId(paymentMethod.getId())
                .promoMethodId(paymentMethod.getId())
                .build();
    }

    DiscountOption calculatePartialLoyaltyPointsPromo(Order order, String defaultPaymentMethod) {
        BigDecimal tenPercentOfOrderValue = calculatePercentageDiscount(order.getValue(), PARTIAL_LOYALTY_POINTS_DISCOUNT_PERCENTAGE);

        return DiscountOption.builder()
                .orderId(order.getId())
                .paymentMethodId(defaultPaymentMethod)
                .promoMethodId(LOYALTY_POINTS_PAYMENT_METHOD_ID)
                .discount(tenPercentOfOrderValue)
                .promoLimitUsed(tenPercentOfOrderValue)
                .promoType(PromotionType.LOYALTY_POINTS_PARTIAL)
                .valueAfterDiscount(order.getValue().subtract(tenPercentOfOrderValue).subtract(tenPercentOfOrderValue))
                .build();
    }

    DiscountOption calculateFullLoyaltyPointsPromo(Order order, PaymentMethod paymentMethod) {
        BigDecimal fullyPaidDiscount = calculatePossibleDiscount(order.getValue(), paymentMethod);
        BigDecimal valueAfterDiscount = order.getValue().subtract(fullyPaidDiscount);

        return DiscountOption.builder()
                .orderId(order.getId())
                .paymentMethodId(LOYALTY_POINTS_PAYMENT_METHOD_ID)
                .promoMethodId(LOYALTY_POINTS_PAYMENT_METHOD_ID)
                .discount(fullyPaidDiscount)
                .promoLimitUsed(valueAfterDiscount)
                .valueAfterDiscount(valueAfterDiscount)
                .promoType(PromotionType.LOYALTY_POINTS_FULL)
                .build();
    }

    BigDecimal calculatePossibleDiscount(BigDecimal orderValue, PaymentMethod paymentMethod) {
        BigDecimal discountPercentage = BigDecimal.valueOf(paymentMethod.getDiscount());
        return calculatePercentageDiscount(orderValue, discountPercentage);
    }

    BigDecimal calculatePercentageDiscount(BigDecimal orderValue, BigDecimal discountPercentage) {
        return orderValue.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

}
