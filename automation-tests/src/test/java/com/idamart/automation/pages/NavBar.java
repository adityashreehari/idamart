package com.idamart.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** The top bar that appears on every page (a "component" object rather than a full page). */
public class NavBar extends BasePage {

    private static final By SEARCH = testId("nav-search");
    private static final By CART_BUTTON = testId("nav-cart");
    private static final By CART_COUNT = testId("cart-count");
    private static final By USER_MENU = testId("user-menu");
    private static final By USER_NAME = testId("user-name");
    private static final By LOGOUT = testId("logout");
    private static final By LOGIN_LINK = testId("nav-login");
    private static final By MY_ORDERS = testId("nav-orders");
    private static final By ADMIN_LINK = testId("nav-admin");

    public NavBar(WebDriver driver) {
        super(driver);
    }

    public ProductsPage search(String keyword) {
        type(SEARCH, keyword);
        visible(SEARCH).sendKeys(Keys.ENTER);
        return new ProductsPage(driver).waitForResults();
    }

    public boolean isLoggedIn() {
        return isPresent(USER_NAME);
    }

    public String loggedInName() {
        return text(USER_NAME);
    }

    public void waitUntilLoggedIn() {
        visible(USER_NAME);
    }

    /** 0 when the badge isn't shown (empty cart). */
    public int cartCount() {
        return isPresent(CART_COUNT) ? Integer.parseInt(text(CART_COUNT)) : 0;
    }

    public void waitForCartCount(int expected) {
        wait.until(d -> cartCount() == expected);
    }

    public void clickCart() {
        click(CART_BUTTON);
    }

    public void logout() {
        click(USER_MENU);
        click(LOGOUT);
        visible(LOGIN_LINK);
    }

    public void openMyOrders() {
        click(USER_MENU);
        click(MY_ORDERS);
    }

    /** True if the user menu offers "Admin dashboard" (only for ADMIN accounts). */
    public boolean showsAdminLink() {
        click(USER_MENU);
        boolean shown = isPresent(ADMIN_LINK);
        click(USER_MENU); // close the dropdown again
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOGOUT));
        return shown;
    }
}
