package com.example.demo.Playwright;

import com.example.demo.domain.ComponentRegion;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Component
@Slf4j
public class PlaywrightUtils {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration SHORT_TIMEOUT = Duration.ofSeconds(1);
    public static final Duration LONG_TIMEOUT = Duration.ofSeconds(10);

    public String buildUrl(ComponentRegion region) {
        return String.format("http://%s:%s",
                region.getLink(),
                region.getPort());
    }

    public Browser createBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(Arrays.asList("--start-maximized"))
                .setTimeout(LONG_TIMEOUT.toMillis()));
    }

    public Browser.NewContextOptions configureContext() {
        return new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .setViewportSize(null);
    }

    public void configurePageDefaults(Page page) {
        page.setDefaultTimeout(DEFAULT_TIMEOUT.toMillis());
        page.setDefaultNavigationTimeout(DEFAULT_TIMEOUT.toMillis());

    }

    // ============ MÉTODOS DE UTILIDAD ============

    public void fillFieldWithRetry(Page page, String selector, String value, String fieldName) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                page.locator(selector).fill(value);
                log.info("{} completado exitosamente", fieldName);
                return;
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw new RuntimeException("Error al completar " + fieldName + ": " + e.getMessage(), e);
                }
                log.warn("Intento {} fallado para {}, reintentando...", attempt, fieldName);
                waitBriefly();
            }
        }
    }

    public void clickWithRetry(Page page, String selector, String buttonName) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                page.locator(selector).click(new Locator.ClickOptions()
                        .setTimeout(SHORT_TIMEOUT.toMillis()));
                log.info("{} clickeado exitosamente", buttonName);
                return;
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw new RuntimeException("Error al hacer click en " + buttonName + ": " + e.getMessage(), e);
                }
                log.warn("Intento {} fallado para {}, reintentando...", attempt, buttonName);
                waitBriefly();
            }
        }
    }

    public void clickWithSelectorOptions(Page page, String[] selectors, String elementName) {

        for (String selector : selectors) {
            try {
                Locator locator = page.locator(selector);

                int count = locator.count();
                log.info("Selector {} encontró {} elementos", selector, count);

                for (int i = 0; i < count; i++) {
                    Locator el = locator.nth(i);

                    if (el.isVisible()) {
                        el.click();
                        log.info("{} clickeado con selector visible: {}", elementName, selector);
                        return;
                    }
                }
            } catch (Exception e) {
                log.info("Selector {} falló: {}", selector, e.getMessage());
            }
        }

        throw new RuntimeException("No se pudo encontrar " + elementName + " visible con ningún selector");
    }

    public void fillWithSelectorOptions(Page page, String[] selectors, String value, String fieldName) {
        for (String selector : selectors) {
            try {
                Locator locator = page.locator(selector);

                locator.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(SHORT_TIMEOUT.toMillis()));

                locator.fill(value);
                log.info("{} completado con selector: {}", fieldName, selector);
                return;
            } catch (Exception e) {
                // Continuar con siguiente selector
            }
        }
        throw new RuntimeException("No se pudo completar " + fieldName + " con ningún selector");
    }

    public void waitForAnyText(Page page, String[] texts, String description) {
        for (String text : texts) {
            try {
                page.getByText(text, new Page.GetByTextOptions().setExact(false))
                        .waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(SHORT_TIMEOUT.toMillis()));
                log.info("{} encontrado con texto: {}", description, text);
                return;
            } catch (Exception e) {
                // Continuar con siguiente texto
            }
        }
        throw new RuntimeException("No se encontró " + description);
    }

    public boolean isElementVisible(Page page, String[] selectors) {
        for (String selector : selectors) {
            try {
                Locator locator = page.locator(selector).first();

                if (locator.isVisible()) {
                    log.info("Elemento encontrado y visible con selector: {}", selector);
                    return true;
                }
            } catch (Exception e) {
                // Continuar con siguiente selector
            }
        }
        log.info("Ningún selector encontró un elemento visible");
        return false;
    }
    
    public void waitBriefly() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void closeResources(BrowserContext context, Browser browser) {
        try {
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
            log.warn("Error al cerrar contexto: {}", e.getMessage());
        }

        try {
            if (browser != null) {
                browser.close();
            }
        } catch (Exception e) {
            log.warn("Error al cerrar navegador: {}", e.getMessage());
        }
    }

    public String buildSuccessResponse(String receipt) {
        return String.format("""
            ✅ TRANSACCIÓN REALIZADA EXITOSAMENTE
            📋 RESPUESTA:
            %s
            ⏰ FECHA: %s
            """,
                receipt,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}