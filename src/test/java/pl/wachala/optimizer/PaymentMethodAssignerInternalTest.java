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
        //given
        PaymentMethod m1 = new PaymentMethod("CARD1", 0, new BigDecimal("100"));
        PaymentMethod m2 = new PaymentMethod("PUNKTY", 0, new BigDecimal("1000"));
        PaymentMethod m3 = new PaymentMethod("CARD2", 5, new BigDecimal("200"));

        //when
        String defaultId = assigner.getDefaultPaymentMethod(Arrays.asList(m1, m2, m3));

        //then
        assertEquals("CARD1", defaultId);
    }

    @Test
    void testGetDefaultPaymentMethodWhenNoDefaultThrows() {
        //given
        PaymentMethod points = new PaymentMethod("PUNKTY", 0, new BigDecimal("1000"));

        //when & then
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> assigner.getDefaultPaymentMethod(Collections.singletonList(points)));
        assertTrue(ex.getMessage().contains("Unable to assign payment method"));
    }

    @Test
    void testCalculatePercentageDiscountRounding() {
        //given
        BigDecimal result = assigner.calculatePercentageDiscount(new BigDecimal("123.4567"), new BigDecimal("12.3456"));

        //when
        BigDecimal expected = new BigDecimal("15.24");

        //then
        assertEquals(0, result.compareTo(expected));
    }

    @Test
    void testCalculatePossibleDiscount() {
        //given
        PaymentMethod pm = new PaymentMethod("CARD", 20, new BigDecimal("1000"));

        //when
        BigDecimal discount = assigner.calculatePossibleDiscount(new BigDecimal("250"), pm);

        //then
        assertEquals(0, discount.compareTo(new BigDecimal("50.00")));
    }

    @Test
    void testCalculateCardPromo() {
        //given
        Order order = new Order("O1", new BigDecimal("500"), Collections.emptyList());
        PaymentMethod method = new PaymentMethod("CARD", 15, new BigDecimal("1000"));

        //when
        DiscountOption opt = assigner.calculateCardPromo(order, method);

        //then
        assertEquals("O1", opt.getOrderId());
        assertEquals(0, opt.getDiscount().compareTo(new BigDecimal("75.00")));
        assertEquals(0, opt.getValueAfterDiscount().compareTo(new BigDecimal("425.00")));
        assertEquals("CARD", opt.getPromoMethodId());
        assertEquals(PromotionType.CARD, opt.getPromoType());
    }

    @Test
    void testCalculatePartialLoyaltyPointsPromo() {
        //given
        Order order = new Order("O2", new BigDecimal("200"), Collections.emptyList());

        //when
        DiscountOption opt = assigner.calculatePartialLoyaltyPointsPromo(order, "CARD");

        //then
        assertEquals("O2", opt.getOrderId());
        assertEquals(0, opt.getDiscount().compareTo(new BigDecimal("20.00")));
        assertEquals(0, opt.getValueAfterDiscount().compareTo(new BigDecimal("160.00")));
        assertEquals("PUNKTY", opt.getPromoMethodId());
        assertEquals(PromotionType.LOYALTY_POINTS_PARTIAL, opt.getPromoType());
    }

    @Test
    void testCalculateFullLoyaltyPointsPromo() {
        //given
        Order order = new Order("O3", new BigDecimal("120"), Collections.emptyList());
        PaymentMethod points = new PaymentMethod("PUNKTY", 100, new BigDecimal("1000"));

        //when
        DiscountOption opt = assigner.calculateFullLoyaltyPointsPromo(order, points);

        //then
        assertEquals("O3", opt.getOrderId());
        assertEquals(0, opt.getDiscount().compareTo(new BigDecimal("120.00")));
        assertEquals(0, opt.getValueAfterDiscount().compareTo(BigDecimal.ZERO));
        assertEquals("PUNKTY", opt.getPromoMethodId());
        assertEquals(PromotionType.LOYALTY_POINTS_FULL, opt.getPromoType());
    }

    @Test
    void testGetPossibleDiscountsContainsAllOptions() {
        //given
        Order order = new Order("O4", new BigDecimal("100"), Arrays.asList("CARDX"));
        PaymentMethod card = new PaymentMethod("CARDX", 10, new BigDecimal("1000"));
        PaymentMethod points = new PaymentMethod("PUNKTY", 10, new BigDecimal("1000"));
        HashMap<String, PaymentMethod> map = new HashMap<>();
        map.put("CARDX", card);
        map.put("PUNKTY", points);

        //when
        List<DiscountOption> options = assigner.getPossibleDiscounts(
                Collections.singletonList(order),
                map,
                "DEFAULT"
        );

        //then
        assertEquals(3, options.size());
        assertTrue(options.stream().anyMatch(o -> o.getPromoType() == PromotionType.CARD));
        assertTrue(options.stream().anyMatch(o -> o.getPromoType() == PromotionType.LOYALTY_POINTS_PARTIAL));
        assertTrue(options.stream().anyMatch(o -> o.getPromoType() == PromotionType.LOYALTY_POINTS_FULL));
    }

}
