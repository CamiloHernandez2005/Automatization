package com.example.demo.e2e;

import com.example.demo.Playwright.ProductFlowType;
import com.example.demo.config.TestConfig;
import com.example.demo.e2e.flows.PurchaseFlowRegistry;
import com.example.demo.e2e.pages.LoginPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@DisplayName("PC Terminal E2E")
class PCTerminalTest extends BasePlaywrightTest {

    private String user() {
        return TestConfig.getString("pcterminal.login.username");
    }

    private String password() {
        return TestConfig.getString("pcterminal.login.password");
    }

    private String product() {
        return TestConfig.getString("pcterminal.product");
    }

    @Test
    @DisplayName("Login con contraseña incorrecta debe fallar")
    void loginConContrasenaIncorrecta() {

        new LoginPage(page)
                .navigate(BASE_URL)
                .loginAs(user(), "contrasena-invalida-xyz");

        boolean dashboardVisible = page.locator("text=My Dashboard").isVisible();
        assertFalse(dashboardVisible,
                "Se accedió al dashboard con credenciales inválidas");

        Locator errorMsg = page.locator("span[id='loginForm.errors']");
        errorMsg.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        String textoError = errorMsg.innerText().trim();
        System.out.println("Mensaje de error de login: " + textoError);

    }

    @Test
    @DisplayName("Login con credenciales válidas accede al dashboard")
    void loginCorrecto() {
        new LoginPage(page)
                .navigate(BASE_URL)
                .loginAs(user(), password());

        verificarLoginExitoso();

        assertTrue(page.locator("text=My Dashboard").isVisible(),
                "No se mostró 'My Dashboard' tras un login correcto");
    }

    @Test
    @DisplayName("Compra de producto en PC Terminal con credenciales válidas")
    void compraProductoPCTerminal() {
        String clerkId = TestConfig.getOrDefault("pcterminal.clerkId", "");

        ejecutarFlujoCompra(clerkId);

        Locator modal = page.locator(".modal-body:visible");
        Locator pagePrintLocator = modal.locator(".page-print");
        pagePrintLocator.first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));

        List<String> resultTransaction = pagePrintLocator.allInnerTexts().stream()
                .distinct()
                .toList();

        assertFalse(resultTransaction.isEmpty(),
                "No se obtuvo información de la transacción en .page-print");
        assertTrue(resultTransaction.stream().anyMatch(s -> s != null && !s.isBlank()),
                "El recibo de la transacción está vacío");

        Locator finish = modal.locator("a", new Locator.LocatorOptions().setHasText("Finish"));
        finish.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        finish.click();

        System.out.println("Transaction completed: " + resultTransaction);
    }

    @Test
    @DisplayName("Flujo completo con clerkId erróneo")
    void flujoCompletoConClerkIdErroneo() {
        String clerkIdErroneo = "0000";

        ejecutarFlujoCompra(clerkIdErroneo);

        Locator modal = page.locator(".modal-body:visible");
        Locator pagePrintLocator = modal.locator(".page-print");
        pagePrintLocator.first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));

        List<String> resultTransaction = pagePrintLocator.allInnerTexts().stream()
                .distinct()
                .toList();

        Locator finish = modal.locator("a", new Locator.LocatorOptions().setHasText("Finish"));
        if (finish.count() > 0 && finish.first().isVisible()) {
            finish.first().click();
        }

        System.out.println("Transaction (clerkId errón  eo): " + resultTransaction);
    }

    /**
     * Tras intentar un login que se espera exitoso: si aparece el span de error
     * del formulario de login, falla el test mostrando el mensaje real. Si no,
     * espera al dashboard.
     */
    private void verificarLoginExitoso() {
        Locator errorMsg = page.locator("span[id='loginForm.errors']");
        if (errorMsg.count() > 0 && errorMsg.first().isVisible()) {
            String texto = errorMsg.first().innerText().trim();
            fail("Login falló con mensaje: \"" + texto + "\"");
        }
        page.waitForSelector("text=My Dashboard");
    }

    private void ejecutarFlujoCompra(String clerkIdParam) {
        ProductFlowType flowType = resolveFlowType();
        log.info("Ejecutando flujo de compra: {}", flowType);

        new LoginPage(page)
                .navigate(BASE_URL)
                .loginAs(user(), password());

        verificarLoginExitoso();

        page.click("a.icon.wb-search[role=\"button\"]");
        page.fill("input[placeholder='Search Products']", product());
        page.click("#autocomplete-result-0");

        Locator modal = page.locator(".modal-body:visible");
        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        PurchaseFlowRegistry.get(flowType).fillPurchaseForm(modal, clerkIdParam);

        Locator purchaseBtn = modal.locator("a", new Locator.LocatorOptions().setHasText("Purchase"));
        purchaseBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        purchaseBtn.click();
    }

    private ProductFlowType resolveFlowType() {
        String raw = TestConfig.getOrDefault("pcterminal.flowType", "TOP_UP");
        try {
            return ProductFlowType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "pcterminal.flowType inválido: '" + raw + "'. Valores válidos: " +
                            Arrays.toString(ProductFlowType.values()), e);
        }
    }
}
