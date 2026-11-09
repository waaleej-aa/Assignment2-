package com.uet.legacy;

import java.util.Map;

public interface NotificationService {
    void send(Map<String, Object> order, double payable);
}