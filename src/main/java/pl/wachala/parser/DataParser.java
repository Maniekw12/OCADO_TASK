package pl.wachala.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import pl.wachala.models.Order;
import pl.wachala.models.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.wachala.util.Consts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataParser {

    @Autowired
    private final ObjectMapper mapper;

    public List<PaymentMethod> parsePaymentMethods(String paymentMethodsFile) throws IOException {
        List<PaymentMethod> paymentMethods = mapper.readValue(new File(paymentMethodsFile), new TypeReference<>() {
        });

        if (!methodsValidation(paymentMethods)) {
            throw new IllegalArgumentException("Payment methods have to contain " + Consts.LOYALTY_POINTS_PAYMENT_METHOD_ID + " and at least a single other method.");
        }

        return paymentMethods;
    }


    public List<Order> parseOrders(String filePath) throws IOException {
        List<Order> orders = mapper.readValue(new File(filePath), new TypeReference<>() {
        });

        for (Order order : orders) {
            if (order.getPromotions() == null) {
                order.setPromotions(new ArrayList<>());
            }
        }

        return orders;
    }

    private boolean methodsValidation(List<PaymentMethod> paymentMethods) {
        boolean hasPoints = false;
        boolean hasOther = false;

        for (PaymentMethod method : paymentMethods) {
            if (Consts.LOYALTY_POINTS_PAYMENT_METHOD_ID.equalsIgnoreCase(method.getId())) {
                hasPoints = true;
            } else {
                hasOther = true;
            }
        }
        return hasPoints && hasOther;
    }

}
