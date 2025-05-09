package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentOptimizer {

	public static void main(String[] args) {
		SpringApplication.run(PaymentOptimizer.class, args);

		if (args.length > 0) {
			System.out.println("Received arguments:");
			for (String arg : args) {
				System.out.println(arg);
			}
		}
	}

}
