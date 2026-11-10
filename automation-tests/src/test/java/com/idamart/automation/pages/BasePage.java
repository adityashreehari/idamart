package com.idamart.automation.pages;

import com.idamart.automation.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.List;

/**
 * Shared helpers for every page object.
 *
 * Page Object Model: each page of the website gets one class that knows its elements and actions.
 * Tests then read like a story ("login, add to cart, checkout") and if the HTML changes,
 * only the page class needs updating, not every test.
 *
 * Elements are found by data-testid attributes, which were added to the React components
 * especially for testing, so tests don't break when styles or text change.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.TIMEOUT_SECONDS));
    }

    /** By.cssSelector("[data-testid='login-email']") */
    protected static By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    protected void openPath(String path) {
        driver.get(TestConfig.baseUrl() + path);
    }

    /** Explicit wait: keep checking (up to 10 s) until the element is visible. Never Thread.sleep. */
    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> allVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /**
     * Scroll the element to the middle of the screen first, so the sticky navbar
     * never covers it, then click once it's clickable.
     */
    protected void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    protected void type(By locator, String text) {
        WebElement input = visible(locator);
        input.clear();
        input.sendKeys(text);
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected boolean isEnabled(By locator) {
        return visible(locator).isEnabled();
    }

    protected void waitUntilGone(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /** "/products/4" for http://localhost:5173/products/4?x=1 */
    public String currentPath() {
        return URI.create(driver.getCurrentUrl()).getPath();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public void waitForPath(String path) {
        wait.until(d -> currentPath().equals(path));
    }

    /**
     * Waits until a green "success" toast containing this text appears.
     * (Several toasts can be on screen at once, e.g. "Welcome back" plus "Added to cart",
     * so we look through all of them rather than taking the first.)
     */
    public boolean hasSuccessToast(String expectedText) {
        try {
            return wait.until(d -> d.findElements(testId("toast-success")).stream()
                    .anyMatch(t -> t.getText().contains(expectedText)));
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }
}
