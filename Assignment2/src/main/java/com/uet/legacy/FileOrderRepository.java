package com.uet.legacy;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class FileOrderRepository implements OrderRepository {

    private String logFilePath = "order_audit.log";

    @Override
    public void save(String email, double payableAmount, String status) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(LocalDateTime.now() + " | Order for: " + email + " | Payable: PKR "
                         + payableAmount + " | Status: " + status + "\n");
        } catch (IOException e) {
            System.out.println("Failed to write order log: " + e.getMessage());
        }
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getLogFilePath() {
        return logFilePath;
    }
}