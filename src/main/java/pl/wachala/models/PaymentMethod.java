package pl.wachala.models;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PaymentMethod {
    private String id;
    private int discount;
    private BigDecimal limit;
}
