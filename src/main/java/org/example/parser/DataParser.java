package org.example.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.models.Order;
import org.example.models.PaymentMethod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class DataParser {

    public static List<PaymentMethod> parsePaymentMethods(String paymentMethodsFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(paymentMethodsFile), new TypeReference<List<PaymentMethod>>() {});
    }

    public static List<Order> parseOrders(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Order> orders = objectMapper.readValue(new File(filePath), new TypeReference<List<Order>>() {});

        // Ensure promotions list is not null for any order
        for (Order order : orders) {
            if (order.getPromotions() == null) {
                order.setPromotions(new ArrayList<>());
            }
        }

        return orders;
    }

}
