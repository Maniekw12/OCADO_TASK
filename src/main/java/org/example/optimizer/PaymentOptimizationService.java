package org.example.optimizer;

import net.bytebuddy.implementation.bytecode.Throw;
import org.example.models.Order;
import org.example.models.PaymentMethod;
import org.example.models.PaymentPlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentOptimizationService  {
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
        for(Order order : orders) {
            PaymentPlan bestPlan = findBestPlan(order);

            if(bestPlan == null) {
                throw new RuntimeException("Couldn't find the best plan");
            }

            applyPlan(bestPlan);
        }

    }

    private void applyPlan(PaymentPlan bestPlan) {
        for(Map.Entry<String, BigDecimal> entry : bestPlan.getPaymentsByMethod().entrySet()) {
            String methodId = entry.getKey();
            BigDecimal amount = entry.getValue();
            availableLimits.put(methodId, availableLimits.get(methodId).subtract(amount));

            spentPerMethod.put(methodId, spentPerMethod.get(methodId).add(amount));
        }

    }

    private PaymentPlan findBestPlan(Order order){
        BigDecimal orderValue = order.getValue();

        //Case 1 - only points
        PaymentPlan pointsOnlyPlan = tryFullPointsPayment(order);

        //Case 2 - only card
        PaymentPlan cardOnlyPlan = tryFullCardPayment(order);

        //Case 3 - partial points and card
        PaymentPlan pointsAndCard = tryPartialPointsPayment(order);

        //PaymentPlan bestPlan = selectBestPlan(pointsOnlyPlan,cardOnlyPlan,pointsAndCard);
        return null;
    }

    private PaymentPlan tryFullPointsPayment(Order order) {
        if(availableLimits.get(POINTS_ID) == null || availableLimits.get(POINTS_ID).compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }
        BigDecimal orderValue = order.getValue();
        PaymentMethod pointsMethod = methodMap.get(POINTS_ID);
        BigDecimal availablePoints = availableLimits.get(POINTS_ID);

        BigDecimal discountMultiplier = BigDecimal.valueOf(pointsMethod.getDiscount()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discount = orderValue.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amountToPay = orderValue.subtract(discount);

        if(amountToPay.compareTo(availablePoints) <= 0){
            HashMap<String, BigDecimal> payments = new HashMap<>();
            payments.put(POINTS_ID, amountToPay);

            return PaymentPlan.builder()
                    .paymentsByMethod(payments)
                    .totalDiscount(discount)
                    .build();
        }
        return null; // in case there is not enough money available
    }

    private PaymentPlan tryFullCardPayment(Order order) {
        return null;
    }

    private PaymentPlan tryPartialPointsPayment(Order order) {
        return null;
    }

}
