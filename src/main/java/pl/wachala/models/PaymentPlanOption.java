package pl.wachala.models;

import lombok.Getter;
import lombok.Setter;

//TODO consider removal
@Getter
@Setter
public class PaymentPlanOption {
    private Order order;
    private PaymentPlan plan;

    public PaymentPlanOption(Order order, PaymentPlan plan) {
        this.order = order;
        this.plan = plan;
    }
}