package org.example.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentPlanOption {
    Order order;
    PaymentPlan plan;

    public PaymentPlanOption(Order order, PaymentPlan plan) {
        this.order = order;
        this.plan = plan;
    }
}