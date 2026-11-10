package com.idamart.automation.tests;

import com.idamart.automation.base.BaseTest;
import com.idamart.automation.pages.AdminPage;
import com.idamart.automation.pages.NavBar;
import com.idamart.automation.pages.ProductsPage;
import com.idamart.automation.utils.TestData;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AdminTest extends BaseTest {

    @Test(groups = {"smoke"}, description = "TC_ADM_01: Admin sees the dashboard with stats")
    public void adminDashboard() {
        loginAsAdmin();

        assertTrue(new AdminPage(driver).open().isDashboardShown());
    }

    @Test(description = "TC_ADM_03: A customer can't open the admin dashboard")
    public void customerBlockedFromAdmin() {
        registerNewCustomer();
        AdminPage admin = new AdminPage(driver).open();

        admin.waitForPath("/");
        assertFalse(new NavBar(driver).showsAdminLink(), "Customers must not see the Admin link");
    }

    @Test(description = "TC_ADM_04: Admin adds a product, finds it in the shop, then removes it")
    public void adminAddsAndRemovesProduct() {
        String name = TestData.uniqueName("Selenium Kunai Set");
        loginAsAdmin();

        AdminPage admin = new AdminPage(driver).open().openProductsTab()
                .addProduct(name, "Naruto", "Accessories", "25.50", "12");
        assertTrue(admin.hasSuccessToast("Added " + name), "Toast should confirm the new product");

        admin.searchProducts(name, 1);
        assertTrue(new ProductsPage(driver).openWithKeyword(name).productNames().contains(name),
                "New product should be visible in the shop");

        admin = new AdminPage(driver).open().openProductsTab().searchProducts(name, 1).deleteFirstProduct();
        assertEquals(admin.productRowCount(), 0);
        assertTrue(new ProductsPage(driver).openWithKeyword(name).showsNoResults(),
                "Removed product should be gone from the shop");
    }
}
