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
import org.junit.jupiter.params.provider.ValueSource;

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

    private Map<String, Object> orderWith(String key, Object value) {
        Map<String, Object> order = validOrder();
        order.put(key, value);
        return order;
    }

    private void assertRejected(Map<String, Object> order) {
        assertFalse(engine.processOrder(order));
        assertFalse(Files.exists(logFile), "Rejected order must not be logged");
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

    @Test
    void creditCard_valid16Digits_isPaid() throws Exception {
        Map<String, Object> order = orderWith("paymentMethod", "CREDIT_CARD");
        order.put("cardNumber", "1234567890123456");

        assertTrue(engine.processOrder(order));
        assertTrue(Files.readString(logFile).contains("Status: PAID"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123456789012345", "12345678901234567",
        "abcdefghijklmnop", "1234 5678 9012 3456"})
    void creditCard_invalidNumber_isRejected(String card) {
        Map<String, Object> order = orderWith("paymentMethod", "CREDIT_CARD");
        order.put("cardNumber", card);
        assertRejected(order);
    }

    @Test
    void easyPaisa_validMobile_isPaid() throws Exception {
        Map<String, Object> order = orderWith("paymentMethod", "EASYPAISA");
        order.put("mobileNumber", "03001234567");

        assertTrue(engine.processOrder(order));
        assertTrue(Files.readString(logFile).contains("Status: PAID"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "0300123456", "030012345678", "04001234567"})
    void easyPaisa_invalidMobile_isRejected(String mobile) {
        Map<String, Object> order = orderWith("paymentMethod", "EASYPAISA");
        order.put("mobileNumber", mobile);
        assertRejected(order);
    }

    @Test
    void cashOnDelivery_underLimit_isPendingCash() throws Exception {
        // 21000 - 500 flat = 20500; x1.17 = 23985 (under 25000)
        Map<String, Object> order = orderWith("items",
                List.of(Map.<String, Object>of("price", 21000.0, "quantity", 1)));

        assertTrue(engine.processOrder(order));
        assertTrue(Files.readString(logFile).contains("Status: PENDING_CASH"));
    }

    @Test
    void cashOnDelivery_overLimit_isRejected() {
        // 22000 - 500 = 21500; x1.17 = 25155 (over 25000)
        assertRejected(orderWith("items",
                List.of(Map.<String, Object>of("price", 22000.0, "quantity", 1))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"BITCOIN", "cash_on_delivery", "CASH"})
    void unsupportedPaymentMethod_isRejected(String method) {
        assertRejected(orderWith("paymentMethod", method));
    }

    @Test
    void missingPaymentMethod_defaultsToCash_whichIsUnsupported() {
        Map<String, Object> order = validOrder();
        order.remove("paymentMethod");
        assertRejected(order);
    }

    @ParameterizedTest(name = "country {0} -> payable {1}")
    @CsvSource({"PK, 1170.0", "US, 1080.0", "UK, 1200.0", "DE, 1150.0"})
    void taxRate_byCountry(String country, double expected) throws Exception {
        assertTrue(engine.processOrder(orderWith("country", country)));
        assertEquals(expected, loggedPayable(), 0.01);
    }

    @Test
    void nullOrEmptyOrder_isRejected() {
        assertRejected(null);
        assertRejected(new HashMap<>());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ali.example.com", "plainstring"})
    void invalidEmail_isRejected(String email) {
        assertRejected(orderWith("customerEmail", email));
    }

    @Test
    void missingEmail_isRejected() {
        Map<String, Object> order = validOrder();
        order.remove("customerEmail");
        assertRejected(order);
    }

    @Test
    void emptyCart_isRejected() {
        assertRejected(orderWith("items", new ArrayList<>()));
    }

    @Test
    void missingItems_isRejected() {
        Map<String, Object> order = validOrder();
        order.remove("items");
        assertRejected(order);
    }

    @Test
    void itemMissingPriceOrQuantity_isRejected() {
        assertRejected(orderWith("items", List.of(Map.<String, Object>of("quantity", 1))));
        assertRejected(orderWith("items", List.of(Map.<String, Object>of("price", 100.0))));
    }

    @ParameterizedTest(name = "price {0}, quantity {1}")
    @CsvSource({"-50.0, 1", "0.0, 1", "100.0, 0", "100.0, -2"})
    void nonPositivePriceOrQuantity_isRejected(double price, int qty) {
        assertRejected(orderWith("items",
                List.of(Map.<String, Object>of("price", price, "quantity", qty))));
    }

    @Test
    void priceGivenAsInteger_throwsClassCastException() {
        // Legacy bug: (double) cast on an Integer crashes instead of returning false
        Map<String, Object> order = orderWith("items",
                List.of(Map.<String, Object>of("price", 1000, "quantity", 1)));
        assertThrows(ClassCastException.class, () -> engine.processOrder(order));
    }
}
