package pl.wachala.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import pl.wachala.models.Order;
import pl.wachala.models.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            throw new IllegalArgumentException("Lista metod płatności musi zawierać co najmniej jedną metodę 'PUNKTY' oraz co najmniej jedną inną metodę.");
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

    private boolean methodsValidation(List<PaymentMethod> paymentMethods){
        boolean hasPoints = false;
        boolean hasOther = false;

        for (PaymentMethod method : paymentMethods) {
            if ("PUNKTY".equalsIgnoreCase(method.getId())) {
                hasPoints = true;
            } else {
                hasOther = true;
            }
        }
        return hasPoints && hasOther;
    }

}
