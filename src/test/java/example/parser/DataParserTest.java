package example.parser;

import org.example.models.Order;
import org.example.models.PaymentMethod;
import org.example.parser.DataParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataParserTest {
    @Test
    void testParsePaymentMethodsValidFile() throws IOException {
        List<PaymentMethod> paymentMethods = DataParser.parsePaymentMethods("./src/test/resources/paymentMethods.json");
        assertNotNull(paymentMethods);
        assertFalse(paymentMethods.isEmpty());
    }

    @Test
    void testParseOrderValidFile() throws IOException {
        List<Order> orders = DataParser.parseOrders("src/test/resources/orders.json");
        Order order = orders.get(0);
        assertNotNull(order.getId());
    }

    @Test
    void testParsePaymentMethodsFileNotFound() {
        assertThrows(IOException.class, () -> {
            DataParser.parsePaymentMethods("nonexistent.json");
        });
    }

    @Test
    void testParseOrdersFileNotFound() {
        assertThrows(IOException.class, () -> {
            DataParser.parseOrders("nonexistent_orders.json");
        });
    }

    @Test
    void testParseEmptyPaymentMethodsFile() {
        assertThrows(IOException.class, () -> {
            DataParser.parsePaymentMethods("src/test/resources/emptyPaymentMethods.json");
        });
    }


}
