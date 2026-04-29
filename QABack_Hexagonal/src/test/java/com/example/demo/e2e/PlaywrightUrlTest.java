package com.example.demo.e2e;

import com.example.demo.config.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PlaywrightUrlTest {

    static {
        TestConfig.init();
    }

    @Test
    void abrirUrlDebeSerExitoso() {
        String url = TestConfig.getString("app.smokeUrl");
        boolean headed = TestConfig.getOrDefault("playwright.headed", false);

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(!headed))) {

            Page page = browser.newPage();
            Response response = page.navigate(url);

            assertNotNull(response, "No se recibió respuesta al navegar a " + url);
            assertTrue(response.ok(),
                    "La URL " + url + " respondió con estado " + response.status());

            System.out.println("URL abierta correctamente: " + url + " (status " + response.status() + ")");
        } catch (Exception e) {
            fail("No se pudo abrir la URL " + url + ": " + e.getMessage());
        }
    }
}
