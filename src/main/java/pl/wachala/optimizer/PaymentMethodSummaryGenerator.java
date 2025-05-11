package pl.wachala.optimizer;

import pl.wachala.models.DiscountOption;
import pl.wachala.models.PromotionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentMethodSummaryGenerator {

    public void generateSummary(Map<String, DiscountOption> discountAssignment) {
        Map<String, BigDecimal> summary = new HashMap<>();

        for (DiscountOption discountOption : discountAssignment.values()) {
            BigDecimal previous = summary.getOrDefault(discountOption.getPaymentMethodId(), BigDecimal.ZERO);

            if (discountOption.getPromoType() == PromotionType.CARD) {
                summary.put(discountOption.getPaymentMethodId(), previous.add(discountOption.getValueAfterDiscount()));
            } else if (discountOption.getPromoType() == PromotionType.LOYALTY_POINTS_PARTIAL) {
                //update loyalty points used since we partially paid with it
                BigDecimal previousLoyaltyPoints = summary.getOrDefault(discountOption.getPromoMethodId(), BigDecimal.ZERO);
                summary.put(discountOption.getPromoMethodId(), previousLoyaltyPoints.add(discountOption.getPromoLimitUsed()));

                //update card amount used since we partially paid with it
                summary.put(discountOption.getPaymentMethodId(), previous.add(discountOption.getValueAfterDiscount()));
            } else { //fully paid with loyalty points
                BigDecimal previousLoyaltyPoints = summary.getOrDefault(discountOption.getPromoMethodId(), BigDecimal.ZERO);
                summary.put(discountOption.getPromoMethodId(), previousLoyaltyPoints.add(discountOption.getPromoLimitUsed()));
            }
        }

        for (Map.Entry<String, BigDecimal> entry : summary.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

}
