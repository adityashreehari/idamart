package com.idamart.automation.tests;

import com.idamart.automation.base.BaseTest;
import com.idamart.automation.pages.HomePage;
import com.idamart.automation.pages.ProductDetailPage;
import com.idamart.automation.pages.ProductsPage;
import com.idamart.automation.utils.TestData;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** Browsing the shop without logging in. Test case ids match docs/testing/IdaMart-Test-Cases.xlsx. */
public class BrowseTest extends BaseTest {

    @Test(groups = {"smoke"}, description = "TC_HOME_01: Home page shows the banner, the categories and new arrivals")
    public void homePageLoads() {
        HomePage home = new HomePage(driver).open();

        assertTrue(home.isCarouselShown(), "Hero carousel should be visible");
        assertTrue(home.categoryCount() >= 6, "At least the 6 demo categories");
        assertEquals(home.newArrivalCount(), 8, "New arrival cards");
    }

    @Test(groups = {"smoke"}, description = "TC_SHOP_01: Searching from the navbar shows only matching products")
    public void searchFromNavbar() {
        new HomePage(driver).open();
        ProductsPage results = new HomePage(driver).navBar().search("naruto");

        assertTrue(results.resultCount() > 0, "Expected some Naruto products");
        results.cardSubtitles().forEach(line ->
                assertTrue(line.toLowerCase().contains("naruto"), "Unexpected product in results: " + line));
    }

    @Test(description = "TC_SHOP_02: Search with no matches shows the 'no products' message")
    public void searchWithNoResults() {
        ProductsPage results = new ProductsPage(driver).openWithKeyword("zzzz-not-a-product");

        assertTrue(results.showsNoResults(), "'No products match' box should show");
        assertEquals(results.resultCount(), 0);
    }

    @Test(description = "TC_SHOP_03: Category filter shows only that category")
    public void filterByCategory() {
        ProductsPage shop = new ProductsPage(driver).open().filterByCategory("Manga");

        assertTrue(shop.currentUrl().contains("categoryId="), "Filter should be saved in the URL");
        assertFalse(shop.cardSubtitles().isEmpty());
        shop.cardSubtitles().forEach(line -> assertTrue(line.endsWith("Manga"), "Not a manga: " + line));
    }

    @Test(description = "TC_SHOP_04: Sort by price low to high orders the cards by price")
    public void sortByPriceAscending() {
        ProductsPage shop = new ProductsPage(driver).open().sortBy("Price: low to high");

        List<BigDecimal> prices = shop.productPrices();
        List<BigDecimal> sorted = new ArrayList<>(prices);
        sorted.sort(null);
        assertEquals(prices, sorted, "Prices should be in ascending order");
    }

    @Test(description = "TC_PDP_01: An out-of-stock product can't be added to the cart")
    public void outOfStockProductIsDisabled() {
        ProductDetailPage product = new ProductsPage(driver)
                .openWithKeyword("spy x family vol")
                .openProduct(TestData.OUT_OF_STOCK_PRODUCT);

        assertEquals(product.stockBadge(), "Out of stock");
        assertFalse(product.canAddToCart(), "Add to cart should be disabled");
    }

    @Test(description = "TC_PDP_02: A low-stock product shows 'Only N left'")
    public void lowStockBadge() {
        ProductDetailPage product = new ProductsPage(driver)
                .openWithKeyword("plus ultra")
                .openProduct(TestData.LOW_STOCK_PRODUCT);

        assertTrue(product.stockBadge().startsWith("Only"), "Badge was: " + product.stockBadge());
    }

    @Test(description = "TC_PDP_03: A product id that doesn't exist shows 'Product not found'")
    public void unknownProductShowsNotFound() {
        ProductDetailPage page = new ProductDetailPage(driver).openById(999999);

        assertTrue(page.isNotFound());
    }
}
