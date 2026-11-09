package com.uet.legacy;

import java.util.Map;

public class CashOnDeliveryPayment implements PaymentStrategy {

    private static final double MAX_PAYABLE = 25000.0;

    @Override
    public boolean validate(Map<String, Object> order, double payable) {
        return payable <= MAX_PAYABLE;
    }

    @Override
    public String status() {
        return "PENDING_CASH";
    }
}