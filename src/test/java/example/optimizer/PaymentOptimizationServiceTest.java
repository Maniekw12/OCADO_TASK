package example.optimizer;

import org.example.models.Order;
import org.example.models.PaymentMethod;
import org.example.optimizer.PaymentOptimizationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentOptimizationServiceTest {

    @Test
    void testOptimizationWithGivenOrdersAndMethods() {
        List<Order> orders = Arrays.asList(
                Order.builder().id("ORDER1").value(new BigDecimal("100.00")).promotions(List.of("mZysk")).build(),
                Order.builder().id("ORDER2").value(new BigDecimal("200.00")).promotions(List.of("BosBankrut")).build(),
                Order.builder().id("ORDER3").value(new BigDecimal("150.00")).promotions(List.of("mZysk", "BosBankrut")).build(),
                Order.builder().id("ORDER4").value(new BigDecimal("50.00")).build()
        );

        List<PaymentMethod> methods = Arrays.asList(
                PaymentMethod.builder().id("PUNKTY").discount(15).limit(new BigDecimal("100.00")).build(),
                PaymentMethod.builder().id("mZysk").discount(10).limit(new BigDecimal("180.00")).build(),
                PaymentMethod.builder().id("BosBankrut").discount(5).limit(new BigDecimal("200.00")).build()
        );

        PaymentOptimizationService service = new PaymentOptimizationService(orders, methods);
        service.optimize();

        assertEquals(new BigDecimal("165.00"), service.getSpentPerMethod().get("mZysk"));
        assertEquals(new BigDecimal("190.00"), service.getSpentPerMethod().get("BosBankrut"));
        assertEquals(new BigDecimal("100.00"), service.getSpentPerMethod().get("PUNKTY"));
    }
}
