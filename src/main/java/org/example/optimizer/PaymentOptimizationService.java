package org.example.optimizer;

import lombok.Getter;
import org.example.models.Order;
import org.example.models.PaymentMethod;
import org.example.models.PaymentPlan;
import org.example.models.PaymentPlanOption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Getter
public class PaymentOptimizationService {
    private static final String POINTS_ID = "PUNKTY";
    private static final int PARTIAL_POINTS_DISCOUNT = 10;

    private Map<String, BigDecimal> availableLimits;
    private Map<String, BigDecimal> spentPerMethod;
    private List<Order> orders;
    private List<PaymentMethod> paymentMethods;
    private HashMap<String, PaymentMethod> methodMap;

    public PaymentOptimizationService(List<Order> orders, List<PaymentMethod> paymentMethods) {
        this.orders = orders;
        this.paymentMethods = paymentMethods;

        availableLimits = new HashMap<>();
        spentPerMethod = new HashMap<>();
        methodMap = new HashMap<>();

        for (PaymentMethod method : paymentMethods) {
            availableLimits.put(method.getId(), method.getLimit());
            spentPerMethod.put(method.getId(), BigDecimal.ZERO);
            methodMap.put(method.getId(), method);
        }
    }

    public void optimize() {
        List<PaymentPlanOption> promoOptions = generateAllPromotionPlansSortedByDiscount();

        Set<String> usedOrders = new HashSet<>();
        for (PaymentPlanOption option : promoOptions) {
            if (!usedOrders.contains(option.getOrder().getId()) && canAfford(option.getPlan())) {
                applyPlan(option.getPlan());
                usedOrders.add(option.getOrder().getId());
            }
        }

        for (Order order : orders) {
            if (usedOrders.contains(order.getId())) continue;
            PaymentPlan fallback = tryFullPointsPayment(order);
            if (fallback == null) fallback = tryPartialPointsPayment(order);
            if (fallback == null) fallback = tryFullCardPaymentWithoutPromo(order);
            if (fallback == null) {
                throw new RuntimeException("Nie udało się przetworzyć zamówienia: " + order.getId());
            }
            applyPlan(fallback);
            usedOrders.add(order.getId());
        }
    }

    private boolean canAfford(PaymentPlan plan) {
        for (Map.Entry<String, BigDecimal> entry : plan.getPaymentsByMethod().entrySet()) {
            String methodId = entry.getKey();
            BigDecimal amount = entry.getValue();
            if (availableLimits.getOrDefault(methodId, BigDecimal.ZERO).compareTo(amount) < 0) {
                return false;
            }
        }
        return true;
    }

    private void applyPlan(PaymentPlan bestPlan) {
        for (Map.Entry<String, BigDecimal> entry : bestPlan.getPaymentsByMethod().entrySet()) {
            String methodId = entry.getKey();
            BigDecimal amount = entry.getValue();
            availableLimits.put(methodId, availableLimits.get(methodId).subtract(amount));
            spentPerMethod.put(methodId, spentPerMethod.get(methodId).add(amount));
        }
    }

    private List<PaymentPlanOption> generateAllPromotionPlansSortedByDiscount() {
        List<PaymentPlanOption> options = new ArrayList<>();
        for (Order order : orders) {
            BigDecimal orderValue = order.getValue();
            List<String> promotions = order.getPromotions();

            if (promotions != null) {
                for (String promoId : promotions) {
                    if (POINTS_ID.equals(promoId)) continue;
                    PaymentMethod method = methodMap.get(promoId);
                    if (method == null) continue;
                    BigDecimal discount = orderValue
                            .multiply(BigDecimal
                                    .valueOf(method.getDiscount())
                                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                            .setScale(2, RoundingMode.HALF_UP);

                    BigDecimal amountToPay = orderValue.subtract(discount);
                    BigDecimal available = availableLimits.getOrDefault(promoId, BigDecimal.ZERO);

                    if (available.compareTo(amountToPay) >= 0) {
                        HashMap<String, BigDecimal> payments = new HashMap<>();
                        payments.put(promoId, amountToPay);
                        PaymentPlan plan = PaymentPlan.builder()
                                .paymentsByMethod(payments)
                                .totalDiscount(discount)
                                .build();
                        options.add(new PaymentPlanOption(order, plan));
                    }
                }
            }

            PaymentMethod pointsMethod = methodMap.get(POINTS_ID);
            if (pointsMethod != null) {
                BigDecimal ptsDiscount = orderValue
                        .multiply(BigDecimal.valueOf(pointsMethod.getDiscount())
                                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal amountToPayPts = orderValue.subtract(ptsDiscount);
                BigDecimal availablePts = availableLimits.getOrDefault(POINTS_ID, BigDecimal.ZERO);
                if (availablePts.compareTo(amountToPayPts) >= 0) {
                    HashMap<String, BigDecimal> payments = new HashMap<>();
                    payments.put(POINTS_ID, amountToPayPts);
                    PaymentPlan plan = PaymentPlan.builder()
                            .paymentsByMethod(payments)
                            .totalDiscount(ptsDiscount)
                            .build();
                    options.add(new PaymentPlanOption(order, plan));
                }
            }
        }
        options.sort((a, b) -> b.getPlan().getTotalDiscount().compareTo(a.getPlan().getTotalDiscount()));
        return options;
    }

    private PaymentPlan tryFullCardPaymentWithoutPromo(Order order) {
        BigDecimal orderValue = order.getValue();

        for (PaymentMethod method : paymentMethods) {
            if (method.getId().equals(POINTS_ID)) continue;
            BigDecimal availableLimit = availableLimits.get(method.getId());

            if (availableLimit != null && availableLimit.compareTo(orderValue) >= 0) {
                HashMap<String, BigDecimal> payments = new HashMap<>();
                payments.put(method.getId(), orderValue);

                return PaymentPlan.builder()
                        .paymentsByMethod(payments)
                        .totalDiscount(BigDecimal.ZERO)
                        .build();
            }
        }
        return null;
    }

    private PaymentPlan tryFullPointsPayment(Order order) {
        if (availableLimits.get(POINTS_ID) == null || availableLimits.get(POINTS_ID).compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }
        BigDecimal orderValue = order.getValue();
        PaymentMethod pointsMethod = methodMap.get(POINTS_ID);
        BigDecimal availablePoints = availableLimits.get(POINTS_ID);

        BigDecimal discountMultiplier = BigDecimal.valueOf(pointsMethod.getDiscount()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discount = orderValue.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amountToPay = orderValue.subtract(discount);

        if (amountToPay.compareTo(availablePoints) <= 0) {
            HashMap<String, BigDecimal> payments = new HashMap<>();
            payments.put(POINTS_ID, amountToPay);

            return PaymentPlan.builder()
                    .paymentsByMethod(payments)
                    .totalDiscount(discount)
                    .build();
        }
        return null;
    }

    private PaymentPlan tryPartialPointsPayment(Order order) {
        if (availableLimits.get(POINTS_ID) == null ||
                availableLimits.get(POINTS_ID).compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }


        BigDecimal bestCardDiscountPct = order.getPromotions() == null
                ? BigDecimal.ZERO
                : order.getPromotions().stream()
                .filter(p -> !p.equals(POINTS_ID))
                .map(methodMap::get)
                .filter(Objects::nonNull)
                .map(PaymentMethod::getDiscount)
                .map(BigDecimal::valueOf)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        if (bestCardDiscountPct.compareTo(
                BigDecimal.valueOf(PARTIAL_POINTS_DISCOUNT)) >= 0) {
            return null;
        }

        BigDecimal orderValue = order.getValue();
        BigDecimal availablePts = availableLimits.get(POINTS_ID);

        BigDecimal minPointsNeeded = orderValue.multiply(BigDecimal.valueOf(0.10))
                .setScale(2, RoundingMode.HALF_UP);

        if (availablePts.compareTo(minPointsNeeded) < 0) {
            return null;
        }

        BigDecimal discount = orderValue
                .multiply(BigDecimal.valueOf(PARTIAL_POINTS_DISCOUNT)
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountedAmount = orderValue.subtract(discount);
        BigDecimal pointsToSpend = minPointsNeeded.max(
                availablePts.min(discountedAmount));
        BigDecimal remainingAmount = discountedAmount.subtract(pointsToSpend);

        for (PaymentMethod method : paymentMethods) {
            if (method.getId().equals(POINTS_ID)) continue;

            BigDecimal limit = availableLimits.get(method.getId());
            if (limit != null && limit.compareTo(remainingAmount) >= 0) {
                HashMap<String, BigDecimal> payments = new HashMap<>();
                payments.put(POINTS_ID, pointsToSpend);
                payments.put(method.getId(), remainingAmount);

                return PaymentPlan.builder()
                        .paymentsByMethod(payments)
                        .totalDiscount(discount)
                        .build();
            }
        }

        return null;
    }

}