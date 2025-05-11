package pl.wachala.models;

import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;

//TODO consider removal
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentPlan {
    private HashMap<String, BigDecimal> paymentsByMethod;
    private BigDecimal totalDiscount;
}
