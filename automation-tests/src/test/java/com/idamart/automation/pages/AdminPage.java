package com.idamart.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/** The admin dashboard and its Products / Orders tabs. */
public class AdminPage extends BasePage {

    private static final By REVENUE_CARD = testId("stat-revenue");
    private static final By TAB_PRODUCTS = testId("admin-tab-products");
    private static final By TAB_ORDERS = testId("admin-tab-orders");

    private static final By ORDER_ROWS = testId("admin-order-row");
    private static final By FILTER_PLACED = testId("filter-PLACED");

    private static final By PRODUCT_SEARCH = testId("admin-product-search");
    private static final By PRODUCT_ROWS = testId("admin-product-row");
    private static final By ADD_PRODUCT = testId("add-product");
    private static final By FORM = testId("product-form");
    private static final By SAVE = testId("product-save");
    private static final By CONFIRM_DELETE = testId("confirm-delete");

    public AdminPage(WebDriver driver) {
        super(driver);
    }

    public AdminPage open() {
        openPath("/admin");
        return this;
    }

    public boolean isDashboardShown() {
        return currentPath().equals("/admin") && !visible(REVENUE_CARD).getText().isBlank();
    }

    // ---------------- Orders tab ----------------

    public AdminPage openOrdersTab() {
        click(TAB_ORDERS);
        visible(ORDER_ROWS);
        return this;
    }

    public AdminPage showPlacedOrdersOnly() {
        click(FILTER_PLACED);
        visible(ORDER_ROWS);
        return this;
    }

    private WebElement orderRow(long orderId) {
        return wait.until(d -> d.findElements(ORDER_ROWS).stream()
                .filter(r -> r.getText().contains("#" + orderId))
                .findFirst().orElse(null));
    }

    public String orderStatus(long orderId) {
        return orderRow(orderId).findElement(testId("order-status")).getText().trim();
    }

    /** Clicks "Mark confirmed / shipped / delivered" for that order and waits for the new status. */
    public AdminPage advanceOrder(long orderId) {
        String before = orderStatus(orderId);
        orderRow(orderId).findElement(testId("advance-order")).click();
        wait.until(d -> !orderStatus(orderId).equals(before));
        return this;
    }

    // ---------------- Products tab ----------------

    public AdminPage openProductsTab() {
        click(TAB_PRODUCTS);
        visible(PRODUCT_ROWS);
        return this;
    }

    public AdminPage addProduct(String name, String anime, String category, String price, String stock) {
        click(ADD_PRODUCT);
        visible(FORM);
        type(testId("pf-name"), name);
        type(testId("pf-anime"), anime);
        new Select(visible(testId("pf-category"))).selectByVisibleText(category);
        type(testId("pf-price"), price);
        type(testId("pf-stock"), stock);
        type(testId("pf-description"), "Created by the Selenium test suite.");
        click(SAVE);
        waitUntilGone(FORM);
        return this;
    }

    /** Types in the search box and waits until the table shows exactly `expected` rows. */
    public AdminPage searchProducts(String keyword, int expected) {
        type(PRODUCT_SEARCH, keyword);
        wait.until(d -> d.findElements(PRODUCT_ROWS).size() == expected);
        return this;
    }

    public int productRowCount() {
        return driver.findElements(PRODUCT_ROWS).size();
    }

    public AdminPage deleteFirstProduct() {
        click(testId("delete-product"));
        click(CONFIRM_DELETE);
        wait.until(d -> d.findElements(PRODUCT_ROWS).isEmpty());
        return this;
    }
}
