package pl.wachala.models;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Order {
    private String id;
    private BigDecimal value;
    private List<String> promotions;
}
