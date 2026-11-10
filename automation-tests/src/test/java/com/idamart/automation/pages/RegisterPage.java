package com.idamart.automation.pages;

import com.idamart.automation.utils.TestData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {

    private static final By FULL_NAME = testId("register-fullName");
    private static final By EMAIL = testId("register-email");
    private static final By PASSWORD = testId("register-password");
    private static final By CONFIRM = testId("register-confirmPassword");
    private static final By SUBMIT = testId("register-submit");
    private static final By SERVER_ERROR = testId("register-error");
    private static final By FIELD_ERRORS = By.cssSelector(".invalid-feedback");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage open() {
        openPath("/register");
        visible(FULL_NAME);
        return this;
    }

    public RegisterPage fill(String fullName, String email, String password, String confirm) {
        type(FULL_NAME, fullName);
        type(EMAIL, email);
        type(PASSWORD, password);
        type(CONFIRM, confirm);
        return this;
    }

    public RegisterPage submit() {
        click(SUBMIT);
        return this;
    }

    /** Fills the form with a valid customer, submits, and waits until logged in. */
    public void register(TestData.Customer c) {
        fill(c.fullName(), c.email(), c.password(), c.password()).submit();
        new NavBar(driver).waitUntilLoggedIn();
    }

    /** The red messages under each invalid field. */
    public java.util.List<String> fieldErrors() {
        return driver.findElements(FIELD_ERRORS).stream().map(e -> e.getText().trim()).toList();
    }

    public String serverError() {
        return text(SERVER_ERROR);
    }
}
