package pl.wachala.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.wachala.models.Order;
import pl.wachala.models.PaymentMethod;
import pl.wachala.parser.DataParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataParserTest {
    private final DataParser parser = new DataParser(new ObjectMapper());

    @Test
    void testParsePaymentMethodsValidFile() throws IOException {
        //given
        PaymentMethod loyaltyPoints = PaymentMethod.builder()
                .id("PUNKTY")
                .discount(15)
                .limit(new BigDecimal("100.00"))
                .build();
        PaymentMethod mZysk = PaymentMethod.builder()
                .id("mZysk")
                .discount(10)
                .limit(new BigDecimal("180.00"))
                .build();
        PaymentMethod bosBankrut = PaymentMethod.builder()
                .id("BosBankrut")
                .discount(5)
                .limit(new BigDecimal("200.00"))
                .build();

        //when
        List<PaymentMethod> paymentMethods = parser.parsePaymentMethods("./src/test/resources/paymentMethods.json");

        //then
        assertNotNull(paymentMethods);
        assertEquals(3, paymentMethods.size());
        assertTrue(paymentMethods.contains(loyaltyPoints));
        assertTrue(paymentMethods.contains(mZysk));
        assertTrue(paymentMethods.contains(bosBankrut));
    }

    @Test
    void testParseOrderValidFile() throws IOException {
        //given
        Order first = Order.builder()
                .id("ORDER1")
                .value(new BigDecimal("100.00"))
                .promotions(Collections.singletonList("mZysk"))
                .build();
        Order second = Order.builder()
                .id("ORDER2")
                .value(new BigDecimal("200.00"))
                .promotions(Collections.singletonList("BosBankrut"))
                .build();
        Order third = Order.builder()
                .id("ORDER3")
                .value(new BigDecimal("150.00"))
                .promotions(Arrays.asList("mZysk", "BosBankrut"))
                .build();
        Order fourth = Order.builder()
                .id("ORDER4")
                .value(new BigDecimal("50.00"))
                .promotions(Collections.emptyList())
                .build();

        //when
        List<Order> orders = parser.parseOrders("src/test/resources/orders.json");

        //then
        assertNotNull(orders);
        assertEquals(4, orders.size());
        assertTrue(orders.contains(first));
        assertTrue(orders.contains(second));
        assertTrue(orders.contains(third));
        assertTrue(orders.contains(fourth));
    }

    @Test
    void testParsePaymentMethodsFileNotFound() {
        assertThrows(IOException.class, () -> {
            parser.parsePaymentMethods("nonexistent.json");
        });
    }

    @Test
    void testParseOrdersFileNotFound() {
        assertThrows(IOException.class, () -> {
            parser.parseOrders("nonexistent_orders.json");
        });
    }

    @Test
    void testParseEmptyPaymentMethodsFile() {
        assertThrows(IOException.class, () -> {
            parser.parsePaymentMethods("src/test/resources/emptyPaymentMethods.json");
        });
    }

    @Test
    void testParsePaymentMethodsMissingPoints() {
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parsePaymentMethods("src/test/resources/paymentMethods_missing_points.json");
        });
    }

    @Test
    void testParsePaymentMethodsOnlyPoints() {
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parsePaymentMethods("src/test/resources/paymentMethods_only_points.json");
        });
    }



}
