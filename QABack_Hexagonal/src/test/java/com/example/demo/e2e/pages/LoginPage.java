package com.example.demo.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public LoginPage navigate(String baseUrl) {
        page.navigate(baseUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        return this;
    }

    public LoginPage fillUsername(String username) {
        usernameField().fill(username);
        return this;
    }

    public LoginPage fillPassword(String password) {
        passwordField().fill(password);
        return this;
    }

    public void submit() {
        submitButton().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public void loginAs(String username, String password) {
        fillUsername(username);
        fillPassword(password);
        submit();
    }

    private Locator usernameField() {
        Locator byName = page.locator(
                "input[name='username'], input[name='user'], input[name='email'], input[id='username'], input[id='user'], input[id='email']");
        if (byName.count() > 0) return byName.first();
        return page.locator("input[type='text'], input[type='email']").first();
    }

    private Locator passwordField() {
        return page.locator("input[type='password']").first();
    }

    private Locator submitButton() {
        Locator byRole = page.getByRole(AriaRole.BUTTON);
        if (byRole.count() > 0) return byRole.first();
        return page.locator("button[type='submit'], input[type='submit']").first();
    }
}
