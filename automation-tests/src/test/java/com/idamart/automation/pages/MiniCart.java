package com.idamart.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** The cart that slides in from the right. */
public class MiniCart extends BasePage {

    private static final By DRAWER = By.cssSelector("[data-testid='mini-cart'].show");
    private static final By ITEMS = testId("mini-cart-item");
    private static final By TOTAL = testId("mini-cart-total");
    private static final By VIEW_CART = testId("view-cart");

    public MiniCart(WebDriver driver) {
        super(driver);
    }

    public MiniCart waitForOpen() {
        visible(DRAWER);
        return this;
    }

    public int itemCount() {
        return driver.findElements(ITEMS).size();
    }

    public String total() {
        return text(TOTAL);
    }

    public CartPage viewCart() {
        click(VIEW_CART);
        return new CartPage(driver).waitForItems();
    }
}
