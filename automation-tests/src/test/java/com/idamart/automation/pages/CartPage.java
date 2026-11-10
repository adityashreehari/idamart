package com.idamart.automation.pages;

import com.idamart.automation.utils.TestData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;

public class CartPage extends BasePage {

    private static final By LINES = testId("cart-line");
    private static final By QTY = testId("cart-qty");
    private static final By QTY_PLUS = testId("cart-qty-plus");
    private static final By REMOVE = testId("cart-remove");
    private static final By EMPTY = testId("cart-empty");
    private static final By CLEAR = testId("clear-cart");
    private static final By SUMMARY_ITEMS = testId("summary-items");
    private static final By SUMMARY_TOTAL = testId("summary-total");
    private static final By CHECKOUT = testId("checkout-button");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public CartPage open() {
        openPath("/cart");
        return this;
    }

    public CartPage waitForItems() {
        visible(LINES);
        return this;
    }

    public int lineCount() {
        return driver.findElements(LINES).size();
    }

    public int itemsInSummary() {
        return Integer.parseInt(text(SUMMARY_ITEMS));
    }

    public BigDecimal total() {
        return TestData.parsePrice(text(SUMMARY_TOTAL));
    }

    /** Clicks + on the first line and waits for the server's updated cart. */
    public CartPage increaseFirstLine() {
        int before = itemsInSummary();
        click(QTY_PLUS);
        wait.until(d -> itemsInSummary() == before + 1);
        return this;
    }

    public int firstLineQuantity() {
        return Integer.parseInt(visible(QTY).getDomProperty("value"));
    }

    public CartPage removeFirstLine() {
        click(REMOVE);
        return this;
    }

    /** "Clear cart" asks "Are you sure?" in a browser alert, which we accept. */
    public CartPage clearCart() {
        click(CLEAR);
        wait.until(d -> {
            try {
                d.switchTo().alert().accept();
                return true;
            } catch (org.openqa.selenium.NoAlertPresentException e) {
                return false;
            }
        });
        return this;
    }

    public boolean isEmpty() {
        return !visible(EMPTY).getText().isBlank();
    }

    public CheckoutPage checkout() {
        click(CHECKOUT);
        return new CheckoutPage(driver).waitForLoad();
    }
}
