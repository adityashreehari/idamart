package com.idamart.automation.tests;

import com.idamart.automation.base.BaseTest;
import com.idamart.automation.pages.CartPage;
import com.idamart.automation.pages.MiniCart;
import com.idamart.automation.pages.NavBar;
import com.idamart.automation.pages.ProductDetailPage;
import com.idamart.automation.pages.ProductsPage;
import com.idamart.automation.utils.TestData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CartTest extends BaseTest {

    /** Runs after BaseTest opens the browser: every cart test starts as a brand-new customer. */
    @BeforeMethod(alwaysRun = true) // TestNG runs BaseTest.openBrowser() first
    public void signUp() {
        registerNewCustomer();
    }

    private ProductDetailPage openInStockProduct() {
        return new ProductsPage(driver).openWithKeyword("hidden leaf").openProduct(TestData.IN_STOCK_PRODUCT);
    }

    @Test(groups = {"smoke"}, description = "TC_CART_01: Adding a product opens the slide-out cart and updates the badge")
    public void addToCart() {
        MiniCart drawer = openInStockProduct().increaseQuantity(1).addToCart();

        assertEquals(drawer.itemCount(), 1, "One line in the drawer");
        assertTrue(drawer.total().contains("70.00"), "2 x 35.00, but was " + drawer.total());
        new NavBar(driver).waitForCartCount(2);
        assertTrue(drawer.hasSuccessToast("Added 2"), "Toast should confirm the add");
    }

    @Test(description = "TC_CART_02: Changing the quantity on the cart page updates the total")
    public void changeQuantity() {
        CartPage cart = openInStockProduct().addToCart().viewCart();
        assertEquals(cart.itemsInSummary(), 1);

        cart.increaseFirstLine();

        assertEquals(cart.firstLineQuantity(), 2);
        assertEquals(cart.total(), TestData.IN_STOCK_PRODUCT_PRICE.multiply(BigDecimal.valueOf(2)));
        new NavBar(driver).waitForCartCount(2);
    }

    @Test(description = "TC_CART_03: Removing the only item leaves an empty cart")
    public void removeItem() {
        CartPage cart = openInStockProduct().addToCart().viewCart();

        cart.removeFirstLine();

        assertTrue(cart.isEmpty(), "Empty-cart message should show");
        new NavBar(driver).waitForCartCount(0);
    }

    @Test(description = "TC_CART_04: 'Clear cart' asks for confirmation and empties the cart")
    public void clearCart() {
        CartPage cart = openInStockProduct().addToCart().viewCart();

        cart.clearCart();

        assertTrue(cart.isEmpty());
    }

    @Test(description = "TC_CART_05: Quick view adds a product without leaving the shop page")
    public void quickViewAddToCart() {
        ProductsPage shop = new ProductsPage(driver).open();

        MiniCart drawer = shop.quickViewFirstProduct().addToCart();

        assertEquals(drawer.itemCount(), 1);
        assertEquals(shop.currentPath(), "/products", "Should still be on the shop page");
    }

    @Test(description = "TC_CART_06: The cart is kept after refreshing the page")
    public void cartSurvivesRefresh() {
        openInStockProduct().addToCart();
        new NavBar(driver).waitForCartCount(1);

        driver.navigate().refresh();

        new NavBar(driver).waitForCartCount(1);
    }
}
