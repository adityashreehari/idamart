package com.idamart.automation.base;

import com.idamart.automation.config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Creates the browser for each test.
 * Selenium Manager (built into Selenium 4) finds or downloads the matching driver,
 * so there's no chromedriver.exe to install by hand.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        boolean headless = TestConfig.headless();

        return switch (TestConfig.browser()) {
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                if (headless) options.addArguments("--headless=new");
                options.addArguments("--window-size=1366,900");
                yield new EdgeDriver(options);
            }
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("-headless");
                options.addArguments("--width=1366", "--height=900");
                yield new FirefoxDriver(options);
            }
            default -> {
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new");
                options.addArguments("--window-size=1366,900");
                yield new ChromeDriver(options);
            }
        };
    }
}
