package com.idamart.automation.tests;

import com.idamart.automation.base.BaseTest;
import com.idamart.automation.pages.AdminPage;
import com.idamart.automation.pages.CheckoutPage;
import com.idamart.automation.pages.MyOrdersPage;
import com.idamart.automation.pages.NavBar;
import com.idamart.automation.pages.OrderPage;
import com.idamart.automation.pages.ProductsPage;
import com.idamart.automation.utils.TestData;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** The full shopping journey, end to end. */
public class CheckoutTest extends BaseTest {

    private CheckoutPage goToCheckoutWithOneItem() {
        return new ProductsPage(driver)
                .openWithKeyword("hidden leaf")
                .openProduct(TestData.IN_STOCK_PRODUCT)
                .addToCart()
                .viewCart()
                .checkout();
    }

    private OrderPage placeOrder() {
        return goToCheckoutWithOneItem()
                .fillDelivery("+971 50 123 4567", "Flat 1204, Marina Tower", "Dubai Marina", "Dubai")
                .placeOrder();
    }

    @Test(groups = {"smoke"}, description = "TC_ORD_01: Customer places an order end to end")
    public void placeOrderEndToEnd() {
        TestData.Customer customer = registerNewCustomer();
        OrderPage order = placeOrder();

        assertEquals(order.status(), "Placed");
        assertEquals(order.itemCount(), 1);
        assertTrue(order.total().contains("35.00"), order.total());
        assertEquals(new NavBar(driver).cartCount(), 0, "Cart should be empty after checkout");

        String orderNumber = "Order #" + order.orderId();
        assertTrue(new MyOrdersPage(driver).open().rowTexts().get(0).contains(orderNumber),
                "Newest order should be first in My orders");
        assertTrue(customer.email().startsWith("selenium."));
    }

    @Test(description = "TC_ORD_02: Checkout form shows errors when delivery details are missing")
    public void checkoutValidation() {
        registerNewCustomer();
        CheckoutPage checkout = goToCheckoutWithOneItem();

        assertEquals(checkout.prefilledName(), "Selenium Tester", "Name should be pre-filled");
        checkout.clickPlaceOrder();

        assertTrue(checkout.fieldErrors().contains("Please enter a valid phone number."), checkout.fieldErrors().toString());
        assertTrue(checkout.currentPath().equals("/checkout"), "Should stay on checkout");
    }

    @Test(description = "TC_ORD_03: Customer cancels an order before it ships")
    public void customerCancelsOrder() {
        registerNewCustomer();
        OrderPage order = placeOrder();
        assertTrue(order.canCancel());

        order.cancel();

        assertEquals(order.status(), "Cancelled");
        assertFalse(order.canCancel(), "Cancel button should disappear");
    }

    @Test(groups = {"smoke"}, description = "TC_ADM_02: Admin moves a new order to Confirmed, and the customer sees it")
    public void adminConfirmsOrder() {
        TestData.Customer customer = registerNewCustomer();
        long orderId = placeOrder().orderId();
        new NavBar(driver).logout();

        loginAsAdmin();
        AdminPage admin = new AdminPage(driver).open().openOrdersTab().showPlacedOrdersOnly();
        admin.advanceOrder(orderId);
        assertEquals(admin.orderStatus(orderId), "Confirmed");

        new NavBar(driver).logout();
        new com.idamart.automation.pages.LoginPage(driver).open().login(customer.email(), customer.password());
        assertEquals(new OrderPage(driver).openById(orderId).status(), "Confirmed");
    }
}
