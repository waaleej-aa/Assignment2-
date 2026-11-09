package com.uet.legacy;

import java.util.HashMap;
import java.util.Map;

public class OrderBillingEngine {

    private final FileOrderRepository repository = new FileOrderRepository();
    private final OrderBillingService service;

    public OrderBillingEngine() {
        Map<String, PaymentStrategy> payments = new HashMap<>();
        payments.put("CREDIT_CARD", new CreditCardPayment());
        payments.put("EASYPAISA", new EasyPaisaPayment());
        payments.put("CASH_ON_DELIVERY", new CashOnDeliveryPayment());

        Map<String, NotificationService> notifiers = new HashMap<>();
        notifiers.put("EMAIL", new EmailNotification());
        notifiers.put("SMS", new SmsNotification());

        service = new OrderBillingService(new TaxCalculator(), new DiscountCalculator(),
                                          payments, notifiers, repository);
    }

    public boolean processOrder(Map<String, Object> orderData) {
        return service.processOrder(orderData);
    }

    public void setLogFilePath(String logFilePath) {
        repository.setLogFilePath(logFilePath);
    }

    public String getLogFilePath() {
        return repository.getLogFilePath();
    }
}