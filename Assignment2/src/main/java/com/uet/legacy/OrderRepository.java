package com.uet.legacy;

public interface OrderRepository {
    void save(String email, double payableAmount, String status);
}