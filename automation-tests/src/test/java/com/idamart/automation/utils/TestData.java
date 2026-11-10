package com.idamart.automation.utils;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/** Test data in one place. Unique emails mean tests can run again and again without clashes. */
public final class TestData {

    private TestData() {
    }

    /** Products from the demo catalogue (backend DataSeeder). */
    public static final String IN_STOCK_PRODUCT = "Hidden Leaf Headband";      // 60 in stock, AED 35.00
    public static final BigDecimal IN_STOCK_PRODUCT_PRICE = new BigDecimal("35.00");
    public static final String OUT_OF_STOCK_PRODUCT = "Spy x Family Vol. 1";    // 0 in stock
    public static final String LOW_STOCK_PRODUCT = "Plus Ultra Training T-Shirt"; // 3 in stock

    public record Customer(String fullName, String email, String password) {
    }

    public static Customer newCustomer() {
        String id = System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextInt(100, 999);
        return new Customer("Selenium Tester", "selenium." + id + "@test.com", "Test1234");
    }

    public static String uniqueName(String prefix) {
        return prefix + " " + System.currentTimeMillis();
    }

    /** "AED 1,234.50" -> 1234.50 */
    public static BigDecimal parsePrice(String text) {
        return new BigDecimal(text.replaceAll("[^0-9.]", ""));
    }
}
