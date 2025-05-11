package pl.wachala.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
public class DiscountOption {
    private String orderId;
    private BigDecimal valueAfterDiscount;
    private BigDecimal discount;
    private BigDecimal promoLimitUsed;
    private PromotionType promoType;
    private String promoMethodId;
    private String paymentMethodId;
}
