package com.idamart.automation.pages;

import com.idamart.automation.utils.TestData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.math.BigDecimal;

public class ProductDetailPage extends BasePage {

    private static final By NAME = testId("detail-name");
    private static final By PRICE = testId("detail-price");
    private static final By STOCK_BADGE = testId("stock-badge");
    private static final By QUANTITY = testId("quantity");
    private static final By PLUS = testId("quantity-plus");
    private static final By ADD_TO_CART = testId("add-to-cart");
    private static final By NOT_FOUND = testId("product-not-found");

    public ProductDetailPage(WebDriver driver) {
        super(driver);
    }

    public ProductDetailPage openById(long id) {
        openPath("/products/" + id);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(NAME),
                ExpectedConditions.visibilityOfElementLocated(NOT_FOUND)));
        return this;
    }

    public ProductDetailPage waitForLoad() {
        visible(NAME);
        return this;
    }

    public String name() {
        return text(NAME);
    }

    public BigDecimal price() {
        return TestData.parsePrice(text(PRICE));
    }

    public String stockBadge() {
        return text(STOCK_BADGE);
    }

    public int quantity() {
        return Integer.parseInt(visible(QUANTITY).getDomProperty("value"));
    }

    public ProductDetailPage increaseQuantity(int times) {
        for (int i = 0; i < times; i++) {
            int before = quantity();
            click(PLUS);
            wait.until(d -> quantity() == before + 1);
        }
        return this;
    }

    public boolean canAddToCart() {
        return isEnabled(ADD_TO_CART);
    }

    /** For a logged-in user: adds and waits for the slide-out cart. */
    public MiniCart addToCart() {
        click(ADD_TO_CART);
        return new MiniCart(driver).waitForOpen();
    }

    /** For a visitor who isn't logged in: the click should lead to the login page. */
    public LoginPage addToCartExpectingLogin() {
        click(ADD_TO_CART);
        wait.until(d -> currentPath().equals("/login"));
        return new LoginPage(driver);
    }

    public boolean isNotFound() {
        return isPresent(NOT_FOUND);
    }
}
