package com.uet.legacy;

import java.util.List;
import java.util.Map;

public class OrderBillingService {

    private final TaxCalculator taxCalculator;
    private final DiscountCalculator discountCalculator;
    private final Map<String, PaymentStrategy> paymentStrategies;
    private final Map<String, NotificationService> notifiers;
    private final OrderRepository repository;

    public OrderBillingService(TaxCalculator taxCalculator,
                               DiscountCalculator discountCalculator,
                               Map<String, PaymentStrategy> paymentStrategies,
                               Map<String, NotificationService> notifiers,
                               OrderRepository repository) {
        this.taxCalculator = taxCalculator;
        this.discountCalculator = discountCalculator;
        this.paymentStrategies = paymentStrategies;
        this.notifiers = notifiers;
        this.repository = repository;
    }

    public boolean processOrder(Map<String, Object> orderData) {
        // 1. Input Validation (Messy nested validation and console error printing)
        if (orderData == null || orderData.isEmpty()) {
            System.out.println("Error: Order data is empty!");
            return false;
        }

        String email = (String) orderData.get("customerEmail");
        if (email == null || !email.contains("@")) {
            System.out.println("Error: Invalid customer email!");
            return false;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
        if (items == null || items.isEmpty()) {
            System.out.println("Error: Cart has no items!");
            return false;
        }

        // 2. Total & Discount Calculation (Long method, primitive types, magic numbers)
        double rawTotal = 0.0;
        for (Map<String, Object> item : items) {
            if (!item.containsKey("price") || !item.containsKey("quantity")) {
                System.out.println("Error: Missing item price or quantity!");
                return false;
            }
            double price = (double) item.get("price");
            int quantity = (int) item.get("quantity");
            if (price <= 0 || quantity <= 0) {
                System.out.println("Error: Invalid price or quantity for item!");
                return false;
            }
            rawTotal += price * quantity;
        }

        String customerType = (String) orderData.getOrDefault("customerType", "REGULAR");
        double discount = discountCalculator.calculate(customerType, rawTotal, items.size());

        double netAmount = rawTotal - discount;

        String country = (String) orderData.getOrDefault("country", "PK");
        double taxRate = taxCalculator.rateFor(country);

        double tax = netAmount * taxRate;
        double payableAmount = netAmount + tax;

        // 3. Payment Processing (Violates Open/Closed Principle)
        String paymentMethod = (String) orderData.getOrDefault("paymentMethod", "CASH");
        PaymentStrategy strategy = paymentStrategies.get(paymentMethod);

        if (strategy == null) {
            System.out.println("Error: Unsupported payment method: " + paymentMethod);
            return false;
        }
        if (!strategy.validate(orderData, payableAmount)) {
            System.out.println("Payment rejected for method: " + paymentMethod);
            return false;
        }
        String paymentStatus = strategy.status();

        // 4. Persist the order
        repository.save(email, payableAmount, paymentStatus);

        // 5. Direct Notification Dispatch (Coupled side-effect)
        String notifyChannel = (String) orderData.getOrDefault("notifyChannel", "EMAIL");
        NotificationService notifier = notifiers.get(notifyChannel);
        if (notifier != null) {
            notifier.send(orderData, payableAmount);
        }

        return true;
    }

}