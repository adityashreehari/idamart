package com.idamart.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class QuickViewModal extends BasePage {

    private static final By MODAL = testId("quick-view");
    private static final By ADD = testId("quick-view-add");

    public QuickViewModal(WebDriver driver) {
        super(driver);
    }

    public QuickViewModal waitForOpen() {
        visible(MODAL);
        return this;
    }

    public MiniCart addToCart() {
        click(ADD);
        return new MiniCart(driver).waitForOpen();
    }
}
