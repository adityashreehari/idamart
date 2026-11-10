package com.idamart.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class MyOrdersPage extends BasePage {

    private static final By ROWS = testId("order-row");

    public MyOrdersPage(WebDriver driver) {
        super(driver);
    }

    public MyOrdersPage open() {
        openPath("/orders");
        visible(ROWS);
        return this;
    }

    public List<String> rowTexts() {
        return driver.findElements(ROWS).stream().map(e -> e.getText()).toList();
    }
}
