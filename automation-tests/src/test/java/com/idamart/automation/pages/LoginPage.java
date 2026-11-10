package com.idamart.automation.pages;

import com.idamart.automation.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By EMAIL = testId("login-email");
    private static final By PASSWORD = testId("login-password");
    private static final By SUBMIT = testId("login-submit");
    private static final By ERROR = testId("login-error");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        openPath("/login");
        visible(EMAIL);
        return this;
    }

    public LoginPage enter(String email, String password) {
        type(EMAIL, email);
        type(PASSWORD, password);
        click(SUBMIT);
        return this;
    }

    /** Logs in and waits until the navbar shows the user's name. */
    public void login(String email, String password) {
        enter(email, password);
        new NavBar(driver).waitUntilLoggedIn();
    }

    public void loginAsAdmin() {
        login(TestConfig.adminEmail(), TestConfig.adminPassword());
    }

    public String errorMessage() {
        return text(ERROR);
    }

    public boolean isShown() {
        return currentPath().equals("/login") && isPresent(EMAIL);
    }
}
