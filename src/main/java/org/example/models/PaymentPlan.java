package org.example.models;

import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentPlan {
    HashMap<String, BigDecimal> paymentsByMethod;
    BigDecimal totalDiscount;
}
