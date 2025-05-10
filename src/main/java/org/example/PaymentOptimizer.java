package org.example;

import org.example.models.Order;
import org.example.models.PaymentMethod;
import org.example.parser.DataParser;
import org.example.util.DataValidator;
import org.example.optimizer.PaymentOptimizationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PaymentOptimizer {

	public static void main(String[] args) {
		DataValidator.ValidateArgsNum(args.length);

		String ordersFile = args[0];
		String paymentMethodsFile = args[1];

		try {
			List<PaymentMethod> paymentMethods = DataParser.parsePaymentMethods(paymentMethodsFile);
			List<Order> orders = DataParser.parseOrders(ordersFile);

			PaymentOptimizationService optimizationService = new PaymentOptimizationService(orders, paymentMethods);

			optimizationService.optimize();

			Map<String, BigDecimal> paymentSummary = optimizationService.getSpentPerMethod();

			System.out.println("\n=== Payment Summary ===");
			for (Map.Entry<String, BigDecimal> entry : paymentSummary.entrySet()) {
				System.out.println(entry.getKey() + " " + entry.getValue().setScale(2, BigDecimal.ROUND_HALF_UP));
			}

		} catch (IOException e) {
			System.out.println("Error reading files: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
