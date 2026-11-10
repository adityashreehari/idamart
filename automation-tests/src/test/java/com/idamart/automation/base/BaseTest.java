package com.idamart.automation.base;

import com.idamart.automation.pages.LoginPage;
import com.idamart.automation.pages.RegisterPage;
import com.idamart.automation.utils.Screenshots;
import com.idamart.automation.utils.TestData;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Every test class extends this.
 * Each test gets a FRESH browser (no leftover login or cart), so tests never depend on each other.
 */
public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void openBrowser() {
        driver = DriverFactory.create();
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            Screenshots.save(driver, result.getTestClass().getRealClass().getSimpleName() + "_" + result.getName());
        }
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- shortcuts used by many tests ----------

    /** Signs up a brand-new customer (unique email every time) and returns their details. */
    protected TestData.Customer registerNewCustomer() {
        TestData.Customer customer = TestData.newCustomer();
        new RegisterPage(driver).open().register(customer);
        return customer;
    }

    protected void loginAsAdmin() {
        new LoginPage(driver).open().loginAsAdmin();
    }
}
