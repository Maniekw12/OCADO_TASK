package pl.wachala.optimizer;

import org.junit.jupiter.api.Test;
import pl.wachala.models.DiscountOption;
import pl.wachala.models.Order;
import pl.wachala.models.PaymentMethod;
import pl.wachala.models.PromotionType;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodAssignerInternalTest {

    private final PaymentMethodAssigner assigner = new PaymentMethodAssigner();

    @Test
    void testGetDefaultPaymentMethodReturnsFirstNonLoyalty() {
        PaymentMethod m1 = new PaymentMethod("CARD1", 0, new BigDecimal("100"));
        PaymentMethod m2 = new PaymentMethod("PUNKTY", 0, new BigDecimal("1000"));
        PaymentMethod m3 = new PaymentMethod("CARD2", 5, new BigDecimal("200"));
        String defaultId = assigner.getDefaultPaymentMethod(Arrays.asList(m1, m2, m3));

        assertEquals("CARD1", defaultId);
    }

    @Test
    void testGetDefaultPaymentMethodWhenNoDefaultThrows() {
        PaymentMethod points = new PaymentMethod("PUNKTY", 0, new BigDecimal("1000"));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> assigner.getDefaultPaymentMethod(Collections.singletonList(points)));
        assertTrue(ex.getMessage().contains("Unable to assign payment method"));
    }

    @Test
    void testCalculatePercentageDiscountRounding() {
        BigDecimal result = assigner.calculatePercentageDiscount(new BigDecimal("123.4567"), new BigDecimal("12.3456"));
        BigDecimal expected = new BigDecimal("15.24");

        assertEquals(0, result.compareTo(expected));
    }

    @Test
    void testCalculatePossibleDiscount() {
        PaymentMethod pm = new PaymentMethod("CARD", 20, new BigDecimal("1000"));
        BigDecimal discount = assigner.calculatePossibleDiscount(new BigDecimal("250"), pm);

        assertEquals(0, discount.compareTo(new BigDecimal("50.00")));
    }

    @Test
    void testCalculateCardPromo() {
        Order order = new Order("O1", new BigDecimal("500"), Collections.emptyList());
        PaymentMethod method = new PaymentMethod("CARD", 15, new BigDecimal("1000"));
        DiscountOption opt = assigner.calculateCardPromo(order, method);

        assertEquals("O1", opt.getOrderId());
        assertEquals(0, opt.getDiscount().compareTo(new BigDecimal("75.00")));
        assertEquals(0, opt.getValueAfterDiscount().compareTo(new BigDecimal("425.00")));
        assertEquals("CARD", opt.getPromoMethodId());
        assertEquals(PromotionType.CARD, opt.getPromoType());
    }

    @Test
    void testCalculatePartialLoyaltyPointsPromo() {
        Order order = new Order("O2", new BigDecimal("200"), Collections.emptyList());
        DiscountOption opt = assigner.calculatePartialLoyaltyPointsPromo(order, "CARD");

        assertEquals("O2", opt.getOrderId());
        assertEquals(0, opt.getDiscount().compareTo(new BigDecimal("20.00")));
        assertEquals(0, opt.getValueAfterDiscount().compareTo(new BigDecimal("160.00")));
        assertEquals("PUNKTY", opt.getPromoMethodId());
        assertEquals(PromotionType.LOYALTY_POINTS_PARTIAL, opt.getPromoType());
    }

    @Test
    void testCalculateFullLoyaltyPointsPromo() {
        Order order = new Order("O3", new BigDecimal("120"), Collections.emptyList());
        PaymentMethod points = new PaymentMethod("PUNKTY", 100, new BigDecimal("1000"));
        DiscountOption opt = assigner.calculateFullLoyaltyPointsPromo(order, points);

        assertEquals("O3", opt.getOrderId());
        assertEquals(0, opt.getDiscount().compareTo(new BigDecimal("120.00")));
        assertEquals(0, opt.getValueAfterDiscount().compareTo(BigDecimal.ZERO));
        assertEquals("PUNKTY", opt.getPromoMethodId());
        assertEquals(PromotionType.LOYALTY_POINTS_FULL, opt.getPromoType());
    }

    @Test
    void testGetPossibleDiscountsContainsAllOptions() {
        Order order = new Order("O4", new BigDecimal("100"), Arrays.asList("CARDX"));
        PaymentMethod card = new PaymentMethod("CARDX", 10, new BigDecimal("1000"));
        PaymentMethod points = new PaymentMethod("PUNKTY", 10, new BigDecimal("1000"));
        HashMap<String, PaymentMethod> map = new HashMap<>();
        map.put("CARDX", card);
        map.put("PUNKTY", points);
        List<DiscountOption> options = assigner.getPossibleDiscounts(
                Collections.singletonList(order),
                map,
                "DEFAULT"
        );

        assertEquals(3, options.size());
        assertTrue(options.stream().anyMatch(o -> o.getPromoType() == PromotionType.CARD));
        assertTrue(options.stream().anyMatch(o -> o.getPromoType() == PromotionType.LOYALTY_POINTS_PARTIAL));
        assertTrue(options.stream().anyMatch(o -> o.getPromoType() == PromotionType.LOYALTY_POINTS_FULL));
    }

}
