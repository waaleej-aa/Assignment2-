package com.uet.legacy;

import java.util.Map;

public class CreditCardPayment implements PaymentStrategy {

    @Override
    public boolean validate(Map<String, Object> order, double payable) {
        Object card = order.get("cardNumber");
        return card instanceof String s && s.matches("\\d{16}");
    }

    @Override
    public String status() {
        return "PAID";
    }
}