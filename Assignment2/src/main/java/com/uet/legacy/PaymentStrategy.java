package com.uet.legacy;

import java.util.Map;

public interface PaymentStrategy {
    boolean validate(Map<String, Object> order, double payable);

    String status();
}