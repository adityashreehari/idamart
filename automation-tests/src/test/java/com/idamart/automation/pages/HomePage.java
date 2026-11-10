package com.idamart.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    private static final By CAROUSEL = testId("hero-carousel");
    private static final By CATEGORY_TILES = testId("category-tile");
    private static final By PRODUCT_CARDS = testId("product-card");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        openPath("/");
        visible(CAROUSEL);
        return this;
    }

    public boolean isCarouselShown() {
        return isPresent(CAROUSEL);
    }

    public int categoryCount() {
        return allVisible(CATEGORY_TILES).size();
    }

    public int newArrivalCount() {
        return allVisible(PRODUCT_CARDS).size();
    }

    public NavBar navBar() {
        return new NavBar(driver);
    }
}
