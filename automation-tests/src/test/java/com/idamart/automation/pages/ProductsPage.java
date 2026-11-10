package com.idamart.automation.pages;

import com.idamart.automation.utils.TestData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** The shop page with search, filters, sorting and the product grid. */
public class ProductsPage extends BasePage {

    private static final By CARDS = testId("product-card");
    private static final By CARD_NAMES = By.cssSelector("[data-testid='product-card'] [data-testid='product-name']");
    private static final By CARD_PRICES = By.cssSelector("[data-testid='product-card'] [data-testid='product-price']");
    private static final By RESULT_COUNT = testId("result-count");
    private static final By NO_RESULTS = testId("no-results");
    private static final By CATEGORY_FILTER = testId("filter-category");
    private static final By SORT_FILTER = testId("filter-sort");
    private static final By QUICK_VIEW_BUTTON = testId("quick-view-button");

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    public ProductsPage open() {
        openPath("/products");
        return waitForResults();
    }

    public ProductsPage openWithKeyword(String keyword) {
        openPath("/products?keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8));
        return waitForResults();
    }

    /** Results are ready when either the "N products found" text or the "no results" box shows. */
    public ProductsPage waitForResults() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(RESULT_COUNT),
                ExpectedConditions.visibilityOfElementLocated(NO_RESULTS)));
        return this;
    }

    public ProductsPage filterByCategory(String categoryName) {
        String before = currentUrl();
        new Select(visible(CATEGORY_FILTER)).selectByVisibleText(categoryName);
        wait.until(d -> !currentUrl().equals(before));
        return waitForResults();
    }

    public ProductsPage sortBy(String optionText) {
        String before = currentUrl();
        new Select(visible(SORT_FILTER)).selectByVisibleText(optionText);
        wait.until(d -> !currentUrl().equals(before));
        return waitForResults();
    }

    public int resultCount() {
        return isPresent(NO_RESULTS) ? 0 : Integer.parseInt(text(RESULT_COUNT).replaceAll("\\D", ""));
    }

    public boolean showsNoResults() {
        return isPresent(NO_RESULTS);
    }

    public List<String> productNames() {
        return driver.findElements(CARD_NAMES).stream().map(e -> e.getText().trim()).toList();
    }

    /** Each card's small grey line, e.g. "Naruto · Accessories". */
    public List<String> cardSubtitles() {
        return driver.findElements(By.cssSelector("[data-testid='product-card'] .anime"))
                .stream().map(e -> e.getText().trim()).toList();
    }

    public List<BigDecimal> productPrices() {
        return driver.findElements(CARD_PRICES).stream().map(e -> TestData.parsePrice(e.getText())).toList();
    }

    public ProductDetailPage openProduct(String name) {
        click(By.xpath("//*[@data-testid='product-name' and normalize-space()=\"" + name + "\"]"));
        return new ProductDetailPage(driver).waitForLoad();
    }

    /** Hover the first card so its "Quick view" button appears, then click it. */
    public QuickViewModal quickViewFirstProduct() {
        WebElement firstCard = allVisible(CARDS).get(0);
        new Actions(driver).moveToElement(firstCard).perform();
        WebElement button = firstCard.findElement(QUICK_VIEW_BUTTON);
        wait.until(ExpectedConditions.visibilityOf(button)).click();
        return new QuickViewModal(driver).waitForOpen();
    }
}
