package com.idamart.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** One order: "Thank you" banner, status tracker, items and the Cancel button. */
public class OrderPage extends BasePage {

    private static final By SUCCESS = testId("order-success");
    private static final By TITLE = testId("order-title");
    private static final By STATUS = testId("order-status");
    private static final By TOTAL = testId("order-total");
    private static final By ITEMS = testId("order-item");
    private static final By CANCEL = testId("cancel-order");
    private static final By CONFIRM_CANCEL = testId("confirm-cancel");

    public OrderPage(WebDriver driver) {
        super(driver);
    }

    public OrderPage waitForSuccess() {
        visible(SUCCESS);
        return this;
    }

    public OrderPage openById(long id) {
        openPath("/orders/" + id);
        visible(TITLE);
        return this;
    }

    /** "Order #12" -> 12 */
    public long orderId() {
        return Long.parseLong(text(TITLE).replaceAll("\\D", ""));
    }

    public String status() {
        return text(STATUS);
    }

    public String total() {
        return text(TOTAL);
    }

    public int itemCount() {
        return driver.findElements(ITEMS).size();
    }

    public boolean canCancel() {
        return isPresent(CANCEL);
    }

    public OrderPage cancel() {
        click(CANCEL);
        click(CONFIRM_CANCEL);
        wait.until(d -> status().equals("Cancelled"));
        return this;
    }
}
