package pl.wachala.optimizer;

import org.junit.jupiter.api.Test;
import pl.wachala.models.DiscountOption;
import pl.wachala.models.Order;
import pl.wachala.models.PaymentMethod;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentMethodAssignerTest {
    private final PaymentMethodAssigner assigner = new PaymentMethodAssigner();

    @Test
    void shouldApplyPromotionsBasedOnConditions() {
        // given
        Order order1 = new Order("ORDER1", new BigDecimal("100.00"), Arrays.asList("mZysk"));
        Order order2 = new Order("ORDER2", new BigDecimal("200.00"), Arrays.asList("BosBankrut"));
        Order order3 = new Order("ORDER3", new BigDecimal("150.00"), Arrays.asList("mZysk", "BosBankrut"));
        Order order4 = new Order("ORDER4", new BigDecimal("50.00"), Collections.emptyList());

        List<Order> orders = Arrays.asList(order1, order2, order3, order4);

        PaymentMethod points = new PaymentMethod("PUNKTY", 15, new BigDecimal("100.00"));
        PaymentMethod mZysk = new PaymentMethod("mZysk", 10, new BigDecimal("180.00"));
        PaymentMethod bosBankrut = new PaymentMethod("BosBankrut", 5, new BigDecimal("200.00"));

        List<PaymentMethod> promotions = Arrays.asList(points, mZysk, bosBankrut);

        // when
        Map<String, DiscountOption> result = assigner.assign(orders, promotions);

        // then

        // Test for ORDER1
        assertDiscountOption(result, "ORDER1", new BigDecimal("15.00"), new BigDecimal("85.00"), "PUNKTY");

        // Test for ORDER2
        assertDiscountOption(result, "ORDER2", new BigDecimal("10.00"), new BigDecimal("190.00"), "BosBankrut");

        // Test for ORDER3
        assertDiscountOption(result, "ORDER3", new BigDecimal("15.00"), new BigDecimal("135.00"), "mZysk");

        // Test for ORDER4
        assertDiscountOption(result, "ORDER4", new BigDecimal("5.00"), new BigDecimal("40.00"), "PUNKTY");

    }

    @Test
    void shouldThrowExceptionWhenNoDefaultPaymentMethodAvailable() {
        // given
        Order order1 = new Order("ORDER1", new BigDecimal("100.00"), Arrays.asList("mZysk"));
        Order order2 = new Order("ORDER2", new BigDecimal("200.00"), Arrays.asList("BosBankrut"));
        Order order3 = new Order("ORDER3", new BigDecimal("150.00"), Arrays.asList("mZysk", "BosBankrut"));
        Order order4 = new Order("ORDER4", new BigDecimal("50.00"), Collections.emptyList());

        List<Order> orders = Arrays.asList(order1, order2, order3, order4);

        PaymentMethod points = new PaymentMethod("PUNKTY", 15, new BigDecimal("100.00"));

        List<PaymentMethod> promotions = Arrays.asList(points);

        // when
        // then
        assertThrows(IllegalStateException.class, () -> {
            Map<String, DiscountOption> result = assigner.assign(orders, promotions);
        });

    }

    @Test
    void shouldThrowExceptionWhenLackOfAnyPaymentMethodAvailable() {
        // given
        Order order1 = new Order("ORDER1", new BigDecimal("100.00"), Arrays.asList("mZysk"));
        Order order2 = new Order("ORDER2", new BigDecimal("200.00"), Arrays.asList("BosBankrut"));
        Order order3 = new Order("ORDER3", new BigDecimal("150.00"), Arrays.asList("mZysk", "BosBankrut"));
        Order order4 = new Order("ORDER4", new BigDecimal("50.00"), Collections.emptyList());

        List<Order> orders = Arrays.asList(order1, order2, order3, order4);

        List<PaymentMethod> promotions = Arrays.asList();
        // when
        // then
        assertThrows(IllegalStateException.class, () -> {
            Map<String, DiscountOption> result = assigner.assign(orders, promotions);
        });

    }

    @Test
    void shouldReturnEmptyResultWhenNoOrdersProvided() {
        // given
        List<Order> orders = Collections.emptyList();

        PaymentMethod points = new PaymentMethod("PUNKTY", 15, new BigDecimal("100.00"));
        PaymentMethod mZysk = new PaymentMethod("mZysk", 10, new BigDecimal("180.00"));
        PaymentMethod bosBankrut = new PaymentMethod("BosBankrut", 5, new BigDecimal("200.00"));

        List<PaymentMethod> promotions = Arrays.asList(points, mZysk, bosBankrut);

        // when
        Map<String, DiscountOption> result = assigner.assign(orders, promotions);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldAssignDefaultWhenNoPointsAvailable() {
        Order order = new Order("ORDER1", new BigDecimal("100.00"), List.of("UNKNOWN_CARD"));

        PaymentMethod defaultCard = new PaymentMethod("DEFAULT", 0, new BigDecimal("500.00"));
        PaymentMethod points = new PaymentMethod("PUNKTY", 0, new BigDecimal("0.00"));

        Map<String, DiscountOption> result = assigner.assign(List.of(order), List.of(defaultCard,points));

        DiscountOption opt = result.get("ORDER1");
        assertEquals("DEFAULT", opt.getPromoMethodId());
        assertEquals(BigDecimal.ZERO, opt.getDiscount());
    }

    @Test
    void shouldAssignPointsWhenPointAvailable() {
        Order order = new Order("ORDER1", new BigDecimal("100.00"), List.of("UNKNOWN_CARD"));

        PaymentMethod defaultCard = new PaymentMethod("DEFAULT", 0, new BigDecimal("500.00"));
        PaymentMethod points = new PaymentMethod("PUNKTY", 0, new BigDecimal("1000.00"));

        Map<String, DiscountOption> result = assigner.assign(List.of(order), List.of(defaultCard,points));

        DiscountOption opt = result.get("ORDER1");
        assertEquals("PUNKTY", opt.getPromoMethodId());
        assertEquals(new BigDecimal("0.00"), opt.getDiscount());
    }

    @Test
    void shouldChooseCardWithHigherDiscountWhenBothAreInPromotionList() {
        Order order = new Order("ORDER1", new BigDecimal("100.00"), List.of("BANK1", "BANK2"));

        PaymentMethod bank1 = new PaymentMethod("BANK1", 10, new BigDecimal("100.00"));
        PaymentMethod bank2 = new PaymentMethod("BANK2", 1, new BigDecimal("300.00")); // higher limit
        PaymentMethod defaultCard = new PaymentMethod("PUNKTY", 0, new BigDecimal("10.00"));

        Map<String, DiscountOption> result = assigner.assign(List.of(order), List.of(bank1, bank2, defaultCard));

        assertEquals("BANK1", result.get("ORDER1").getPromoMethodId());
    }

    @Test
    void shouldApplyFullDiscountWhenExactly10PercentPaidWithPoints() {
        // given
        Order order = new Order("ORDER1", new BigDecimal("100.00"), List.of());

        PaymentMethod points = new PaymentMethod("PUNKTY", 100, new BigDecimal("10.00")); // 100% discount dla części punktowej
        PaymentMethod defaultCard = new PaymentMethod("DEFAULT", 0, new BigDecimal("90.00")); // użyta do pozostałej części

        List<PaymentMethod> methods = List.of(points, defaultCard);

        // when
        Map<String, DiscountOption> result = assigner.assign(List.of(order), methods);

        // then
        DiscountOption opt = result.get("ORDER1");
        assertNotNull(opt);
        assertEquals("DEFAULT", opt.getPaymentMethodId());
        assertEquals("PUNKTY", opt.getPromoMethodId());
        assertEquals(new BigDecimal("10.00"), opt.getPromoLimitUsed());
        assertEquals(new BigDecimal("10.00"), opt.getDiscount());
        assertEquals(new BigDecimal("80.00"), opt.getValueAfterDiscount());
    }

    @Test
    void shouldHandleSmallOrderValueGracefully() {
        Order order = new Order("ORDER1", new BigDecimal("0.99"), Collections.emptyList());
        PaymentMethod points = new PaymentMethod("PUNKTY", 10, new BigDecimal("100.00"));
        PaymentMethod defaultCard = new PaymentMethod("DEFAULT", 0, new BigDecimal("500.00"));

        Map<String, DiscountOption> result = assigner.assign(List.of(order), List.of(points, defaultCard));

        DiscountOption opt = result.get("ORDER1");
        assertTrue(opt.getDiscount().compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(new BigDecimal("0.99").subtract(opt.getDiscount()), opt.getValueAfterDiscount());

    }

    @Test
    void testDefaultPaymentMethodAccess() {
        List<PaymentMethod> paymentMethods = List.of(
                new PaymentMethod("CARD1", 0, new BigDecimal("100.00")),
                new PaymentMethod("PUNKTY", 0, new BigDecimal("0.00"))
        );

        String defaultMethod = assigner.getDefaultPaymentMethod(paymentMethods);
        assertEquals("CARD1", defaultMethod);
    }



    private void assertDiscountOption(Map<String, DiscountOption> result, String orderId, BigDecimal expectedDiscount, BigDecimal expectedValueAfterDiscount, String expectedPromoMethodId) {
        assertTrue(result.containsKey(orderId));
        DiscountOption option = result.get(orderId);
        assertTrue(option.getDiscount().compareTo(expectedDiscount) == 0);
        assertTrue(option.getValueAfterDiscount().compareTo(expectedValueAfterDiscount) == 0);
        assertEquals(expectedPromoMethodId, option.getPromoMethodId());
    }

}
