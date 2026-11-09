package com.uet.legacy;

import java.util.Map;

public class EasyPaisaPayment implements PaymentStrategy {

    @Override
    public boolean validate(Map<String, Object> order, double payable) {
        Object mobile = order.get("mobileNumber");
        return mobile instanceof String s && s.matches("03\\d{9}");
    }

    @Override
    public String status() {
        return "PAID";
    }
}