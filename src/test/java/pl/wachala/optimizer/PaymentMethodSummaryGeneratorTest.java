package pl.wachala.optimizer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.wachala.models.DiscountOption;
import pl.wachala.models.PromotionType;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentMethodSummaryGeneratorTest {

    private final PaymentMethodSummaryGenerator generator = new PaymentMethodSummaryGenerator();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }


    @Test
    void shouldGenerateSummaryForCardOnly() {
        // given
        Map<String, DiscountOption> discountAssignment = new HashMap<>();
        DiscountOption cardPayment = DiscountOption.builder()
                .orderId("ORDER1")
                .paymentMethodId("CARD1")
                .promoMethodId("CARD1")
                .promoType(PromotionType.CARD)
                .discount(new BigDecimal("10.00"))
                .promoLimitUsed(BigDecimal.ZERO)
                .valueAfterDiscount(new BigDecimal("90.00"))
                .build();

        discountAssignment.put("ORDER1", cardPayment);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // when
            generator.generateSummary(discountAssignment);
        } finally {
            System.setOut(originalOut); // always restore
        }

        // then
        String expected = "CARD1 90.00";
        assertEquals(expected, outContent.toString().strip());
    }

    @Test
    void shouldGenerateSummaryForPartialPointsPayment() {
        //given
        Map<String, DiscountOption> discountAssignment = new HashMap<>();
        discountAssignment.put("ORDER1", DiscountOption.builder()
                .orderId("ORDER1")
                .paymentMethodId("MASTERCARD")
                .promoMethodId("PUNKTY")
                .promoType(PromotionType.LOYALTY_POINTS_PARTIAL)
                .discount(new BigDecimal("10.00"))
                .promoLimitUsed(new BigDecimal("10.00"))
                .valueAfterDiscount(new BigDecimal("90.00"))
                .build());

        //when
        generator.generateSummary(discountAssignment);

        //then
        String[] lines = outContent.toString().strip().split("\n");
        assertEquals(2, lines.length);

        assertTrue(lines[0].strip().equals("MASTERCARD 90.00") || lines[1].strip().equals("MASTERCARD 90.00"));
        assertTrue(lines[0].strip().equals("PUNKTY 10.00") || lines[1].strip().equals("PUNKTY 10.00"));
    }


}
