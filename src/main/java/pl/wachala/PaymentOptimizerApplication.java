package pl.wachala;

import lombok.extern.slf4j.Slf4j;
import pl.wachala.models.DiscountOption;
import pl.wachala.models.Order;
import pl.wachala.models.PaymentMethod;
import pl.wachala.optimizer.PaymentMethodAssigner;
import pl.wachala.optimizer.PaymentMethodSummaryGenerator;
import pl.wachala.parser.DataParser;
import pl.wachala.util.ArgumentsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Slf4j
@SpringBootApplication
public class PaymentOptimizerApplication implements CommandLineRunner {

    @Autowired
    DataParser parser;

    @Autowired
    ArgumentsValidator argumentsValidator;

    @Autowired
    PaymentMethodAssigner paymentMethodAssigner;

    @Autowired
    PaymentMethodSummaryGenerator paymentMethodSummaryGenerator;

    public static void main(String[] args) {
        SpringApplication.run(PaymentOptimizerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("Validating program arguments");
        argumentsValidator.validateArgsNum(args.length);

        String ordersFilePath = args[0];
        String paymentMethodsFilePath = args[1];

        try {
            log.info("Parsing payment methods from file: {}.", paymentMethodsFilePath);
            List<PaymentMethod> paymentMethods = parser.parsePaymentMethods(paymentMethodsFilePath);
            log.info("Loaded {} payment methods.", paymentMethods.size());

            log.info("Parsing orders from file: {}.", ordersFilePath);
            List<Order> orders = parser.parseOrders(ordersFilePath);
            log.info("Loaded {} orders.", orders.size());

            log.info("Generating solution");
            Map<String, DiscountOption> assignment = paymentMethodAssigner.assign(orders, paymentMethods);
            paymentMethodSummaryGenerator.generateSummary(assignment);
        } catch (IOException e) {
            log.error("Error while reading files: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error while processing files: {}", e.getMessage());
        }
    }
}
