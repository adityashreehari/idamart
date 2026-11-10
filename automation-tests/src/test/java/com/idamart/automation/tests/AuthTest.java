package com.idamart.automation.tests;

import com.idamart.automation.base.BaseTest;
import com.idamart.automation.pages.CartPage;
import com.idamart.automation.pages.LoginPage;
import com.idamart.automation.pages.NavBar;
import com.idamart.automation.pages.ProductsPage;
import com.idamart.automation.pages.RegisterPage;
import com.idamart.automation.utils.TestData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AuthTest extends BaseTest {

    @Test(groups = {"smoke"}, description = "TC_AUTH_01: A new customer can sign up and is logged in straight away")
    public void signUpNewCustomer() {
        TestData.Customer customer = TestData.newCustomer();
        new RegisterPage(driver).open().register(customer);

        NavBar nav = new NavBar(driver);
        assertEquals(nav.loggedInName(), "Selenium");
        assertTrue(nav.hasSuccessToast("Welcome to IdaMart"), "Welcome toast should show");
    }

    /** One test, run once per row: [full name, email, password, confirm, expected message]. */
    @DataProvider(name = "invalidRegistrations")
    public Object[][] invalidRegistrations() {
        return new Object[][]{
                {"A", "good@test.com", "Test1234", "Test1234", "Please enter your full name."},
                {"Test User", "not-an-email", "Test1234", "Test1234", "Please enter a valid email address."},
                {"Test User", "good@test.com", "short1", "short1", "Password must be at least 8 characters."},
                {"Test User", "good@test.com", "lettersonly", "lettersonly", "Password must contain at least one letter and one number."},
                {"Test User", "good@test.com", "Test1234", "Test9999", "Passwords do not match."},
        };
    }

    @Test(dataProvider = "invalidRegistrations",
            description = "TC_AUTH_02: Sign-up form shows the right message for each invalid input")
    public void registerValidation(String name, String email, String password, String confirm, String expected) {
        RegisterPage page = new RegisterPage(driver).open().fill(name, email, password, confirm).submit();

        List<String> errors = page.fieldErrors();
        assertTrue(errors.contains(expected), "Expected '" + expected + "' but got " + errors);
        assertFalse(new NavBar(driver).isLoggedIn(), "Should NOT be logged in");
    }

    @Test(description = "TC_AUTH_03: Signing up twice with the same email is rejected")
    public void duplicateEmailRejected() {
        TestData.Customer customer = registerNewCustomer();
        new NavBar(driver).logout();

        RegisterPage page = new RegisterPage(driver).open()
                .fill("Someone Else", customer.email(), "Test1234", "Test1234").submit();

        assertTrue(page.serverError().contains("already exists"), page.serverError());
    }

    @Test(groups = {"smoke"}, description = "TC_AUTH_04: Admin can log in")
    public void adminLogin() {
        loginAsAdmin();

        NavBar nav = new NavBar(driver);
        assertTrue(nav.isLoggedIn());
        assertTrue(nav.showsAdminLink(), "Admin should see the Admin dashboard link");
    }

    @Test(description = "TC_AUTH_05: Wrong password shows an error and doesn't log in")
    public void wrongPassword() {
        LoginPage login = new LoginPage(driver).open().enter("admin@idamart.com", "WrongPassword1");

        assertEquals(login.errorMessage(), "Invalid email or password");
        assertFalse(new NavBar(driver).isLoggedIn());
    }

    @Test(description = "TC_AUTH_06: Opening /cart while logged out redirects to login, then back to the cart")
    public void protectedPageRedirect() {
        new CartPage(driver).open();
        LoginPage login = new LoginPage(driver);
        login.waitForPath("/login");
        assertTrue(login.currentUrl().contains("redirect=%2Fcart"), login.currentUrl());

        login.loginAsAdmin();
        login.waitForPath("/cart");
    }

    @Test(description = "TC_AUTH_07: Logging out hides the user menu and the cart badge")
    public void logout() {
        registerNewCustomer();
        NavBar nav = new NavBar(driver);
        nav.logout();

        assertFalse(nav.isLoggedIn());
        assertEquals(nav.cartCount(), 0);
    }

    @Test(description = "TC_AUTH_08: A visitor clicking 'Add to cart' is sent to log in first")
    public void addToCartNeedsLogin() {
        new ProductsPage(driver)
                .openWithKeyword("hidden leaf")
                .openProduct(TestData.IN_STOCK_PRODUCT)
                .addToCartExpectingLogin();

        assertTrue(new LoginPage(driver).isShown());
    }
}
