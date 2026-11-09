package com.uet.legacy;

import java.util.Map;

public class EmailNotification implements NotificationService {

    @Override
    public void send(Map<String, Object> order, double payable) {
        String email = (String) order.get("customerEmail");
        System.out.println("[EMAIL DISPATCHED] To: " + email
                + " | Your order of PKR " + String.format("%.2f", payable) + " is confirmed!");
    }
}