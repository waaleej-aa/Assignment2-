package com.uet.legacy;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OrderBillingEngineTest {

    @TempDir
    Path tempDir;

    OrderBillingEngine engine;
    Path logFile;

    @BeforeEach
    void setUp() {
        engine = new OrderBillingEngine();
        logFile = tempDir.resolve("test.log");
        engine.setLogFilePath(logFile.toString());
    }

    private Map<String, Object> validOrder() {
        Map<String, Object> order = new HashMap<>();
        order.put("customerEmail", "ali@example.com");
        order.put("items", List.of(Map.<String, Object>of("price", 1000.0, "quantity", 1)));
        order.put("paymentMethod", "CASH_ON_DELIVERY");
        return order;
    }

    private Map<String, Object> orderOf(String type, double price, int itemCount) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add(Map.<String, Object>of("price", price, "quantity", 1));
        }
        Map<String, Object> order = validOrder();
        order.put("customerType", type);
        order.put("items", items);
        return order;
    }

    private double loggedPayable() throws Exception {
        Matcher m = Pattern.compile("Payable: PKR ([0-9.]+)").matcher(Files.readString(logFile));
        assertTrue(m.find(), "No payable amount found in log");
        return Double.parseDouble(m.group(1));
    }

    @Test
    void regularOrder_cashOnDelivery_succeedsAndIsLogged() throws Exception {
        boolean result = engine.processOrder(validOrder());

        assertTrue(result);
        String log = Files.readString(logFile);
        assertTrue(log.contains("ali@example.com"));
        assertTrue(log.contains("Status: PENDING_CASH"));
    }

    @ParameterizedTest(name = "{0}: {2} item(s) of {1} -> payable {3}")
    @CsvSource({
        "REGULAR,   1000,  1, 1170.0", // no discount
        "REGULAR,  10000,  1, 11700.0", // exactly 10000: NOT over the limit
        "REGULAR,  12000,  1, 13455.0", // flat 500 off
        "VIP,       5000,  1, 5265.0", // exactly 5000: 10%
        "VIP,       6000,  1, 5967.0", // over 5000: 15%
        "STUDENT,   1000,  1, 1076.4", // 8%
        "CORPORATE, 1000,  1, 1111.5", // 5%
        "CORPORATE,  100,  9, 1000.35", // 9 items: still 5%
        "CORPORATE,  100, 10, 936.0" // 10 items: 20%
    })
    void discountRules_produceExpectedPayable(String type, double price, int count, double expected)
            throws Exception {
        assertTrue(engine.processOrder(orderOf(type, price, count)));
        assertEquals(expected, loggedPayable(), 0.01);
    }
}
