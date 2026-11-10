package com.idamart.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class CheckoutPage extends BasePage {

    private static final By NAME = testId("checkout-name");
    private static final By PHONE = testId("checkout-phone");
    private static final By ADDRESS = testId("checkout-address");
    private static final By AREA = testId("checkout-area");
    private static final By EMIRATE = testId("checkout-emirate");
    private static final By PLACE_ORDER = testId("place-order");
    private static final By FIELD_ERRORS = By.cssSelector(".invalid-feedback");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutPage waitForLoad() {
        visible(PLACE_ORDER);
        return this;
    }

    public String prefilledName() {
        return visible(NAME).getDomProperty("value");
    }

    public CheckoutPage fillDelivery(String phone, String address, String area, String emirate) {
        type(PHONE, phone);
        type(ADDRESS, address);
        type(AREA, area);
        new Select(visible(EMIRATE)).selectByVisibleText(emirate);
        return this;
    }

    public CheckoutPage clickPlaceOrder() {
        click(PLACE_ORDER);
        return this;
    }

    public OrderPage placeOrder() {
        clickPlaceOrder();
        return new OrderPage(driver).waitForSuccess();
    }

    public List<String> fieldErrors() {
        return driver.findElements(FIELD_ERRORS).stream().map(e -> e.getText().trim()).toList();
    }
}
