package com.example.demo.e2e;

import com.example.demo.config.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class BasePlaywrightTest {

    static {
        // Ensure config is loaded (and system properties promoted) before any
        // Playwright class is touched, so PlaywrightUtils picks up the timeouts.
        TestConfig.init();
    }

    protected static final String BASE_URL = TestConfig.getString("app.baseUrl");

    protected static Playwright playwright;
    protected static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        boolean headed = TestConfig.getOrDefault("playwright.headed", false);
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(!headed));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }
}
