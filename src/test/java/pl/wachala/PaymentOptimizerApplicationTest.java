package pl.wachala;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wachala.models.DiscountOption;
import pl.wachala.models.Order;
import pl.wachala.models.PaymentMethod;
import pl.wachala.optimizer.PaymentMethodAssigner;
import pl.wachala.optimizer.PaymentMethodSummaryGenerator;
import pl.wachala.parser.DataParser;
import pl.wachala.util.ArgumentsValidator;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOptimizerApplicationTest {

    @InjectMocks
    private PaymentOptimizerApplication application;

    @Mock
    private DataParser dataParser;

    @Spy
    private ArgumentsValidator argumentsValidator;

    @Mock
    private PaymentMethodAssigner paymentMethodAssigner;

    @Mock
    private PaymentMethodSummaryGenerator paymentMethodSummaryGenerator;

    @Test
    void testApplicationRun() throws Exception {
        // given
        String[] args = {"orders.csv", "payments.csv"};
        List<Order> mockOrders = List.of(Order.builder()
                .id("ORDER1")
                .value(new BigDecimal("200.00"))
                .promotions(Arrays.asList("xBank", "zBank"))
                .build());
        List<PaymentMethod> mockMethods = List.of(
                PaymentMethod.builder()
                        .limit(new BigDecimal("300.00"))
                        .discount(5)
                        .id("xBank")
                        .build(),
                PaymentMethod.builder()
                        .limit(new BigDecimal("20.00"))
                        .discount(8)
                        .id("xBank")
                        .build(),
                PaymentMethod.builder()
                        .limit(new BigDecimal("200.00"))
                        .discount(10)
                        .id("PUNKTY")
                        .build()
        );
        Map<String, DiscountOption> mockAssignment = Map.of("order1", DiscountOption.builder()
                .paymentMethodId("PUNKTY")
                .promoLimitUsed(new BigDecimal("180.00"))
                .valueAfterDiscount(new BigDecimal("0.00"))
                .discount(new BigDecimal("20"))
                .orderId("ORDER1")
                .build()
        );

        when(dataParser.parseOrders("orders.csv")).thenReturn(mockOrders);
        when(dataParser.parsePaymentMethods("payments.csv")).thenReturn(mockMethods);
        when(paymentMethodAssigner.assign(mockOrders, mockMethods)).thenReturn(mockAssignment);

        // when
        application.run(args);

        // then
        verify(argumentsValidator).validateArgsNum(2);
        verify(dataParser).parseOrders("orders.csv");
        verify(dataParser).parsePaymentMethods("payments.csv");
        verify(paymentMethodAssigner).assign(mockOrders, mockMethods);
        verify(paymentMethodSummaryGenerator).generateSummary(mockAssignment);
    }

    @Test
    void contextLoads() {
    }

}
