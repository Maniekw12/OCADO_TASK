package org.example.models;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethod {
    private String id;
    private int discount;
    private BigDecimal limit;
}
