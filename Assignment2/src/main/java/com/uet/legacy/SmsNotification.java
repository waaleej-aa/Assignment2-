package com.uet.legacy;

import java.util.Map;

public class SmsNotification implements NotificationService {

    private static final String DEFAULT_PHONE = "0300-0000000";

    @Override
    public void send(Map<String, Object> order, double payable) {
        String phone = (String) order.getOrDefault("phone", DEFAULT_PHONE);
        System.out.println("[SMS DISPATCHED] To: " + phone
                + " | Order confirmed! Paid: PKR " + String.format("%.2f", payable));
    }
}