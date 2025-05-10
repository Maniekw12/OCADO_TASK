package org.example;

import org.example.models.Order;
import org.example.models.PaymentMethod;
import org.example.parser.DataParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;

public class PaymentOptimizer {

	public static void main(String[] args) {
		if (args.length < 2) {
			return;
		}

		String ordersFile = args[0];
		String paymentMethodsFile = args[1];

		try {
			List<PaymentMethod> paymentMethods = DataParser.parsePaymentMethods(paymentMethodsFile);
			List<Order> orders = DataParser.parseOrders(ordersFile);

			System.out.println("=== Payment Methods ===");
			for (PaymentMethod method : paymentMethods) {
				System.out.println(method.getId() + " | " + method.getDiscount() + "% | Limit: " + method.getLimit());
			}

			System.out.println("\n=== Orders ===");
			for (Order order : orders) {
				System.out.println(order.getId() + " | Value: " + order.getValue() + " | Promotions: " + order.getPromotions());
			}

		} catch (IOException e) {
			System.out.println("Error reading files: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
