package com.idamart.automation.config;

/**
 * Settings for a test run. Every value can be changed without touching code, e.g.:
 *   mvn test -Dbrowser=edge -Dheadless=true -DbaseUrl=https://my-live-site.com
 */
public final class TestConfig {

    private TestConfig() {
    }

    public static final int TIMEOUT_SECONDS = 10;

    /** Where the website runs (the React dev server by default). */
    public static String baseUrl() {
        return System.getProperty("baseUrl", "http://localhost:5173").replaceAll("/$", "");
    }

    /** chrome (default), edge or firefox. */
    public static String browser() {
        return System.getProperty("browser", "chrome").toLowerCase();
    }

    /** true = run without opening a visible window (faster, used on servers). */
    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", "false"));
    }

    /** The admin account the backend creates on startup. */
    public static String adminEmail() {
        return System.getProperty("adminEmail", "admin@idamart.com");
    }

    public static String adminPassword() {
        return System.getProperty("adminPassword", "Admin@12345");
    }
}
