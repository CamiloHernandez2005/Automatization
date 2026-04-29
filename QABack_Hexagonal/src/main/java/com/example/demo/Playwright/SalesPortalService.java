package com.example.demo.Playwright;

import com.example.demo.application.componentsRegions.ports.out.ComponentRegionRepositoryPort;
import com.example.demo.domain.ComponentRegion;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class SalesPortalService {
    private final ComponentRegionRepositoryPort regionRepository;
    private final PlaywrightUtils util;

    @Autowired
    public SalesPortalService(ComponentRegionRepositoryPort regionRepository, PlaywrightUtils util) {
        this.regionRepository = regionRepository;
        this.util = util;
    }

    /**
     * Convenience constructor for use outside of the Spring container — e.g. from
     * the programmatic TestRunner — when the URL is already known and no DB
     * resolution of {@link ComponentRegion} is needed. Calling
     * {@link #runSalesPortalTest(TestDTO)} on an instance built with this
     * constructor will throw because there is no repository to resolve the region.
     * Use {@link #runSalesPortalTest(TestDTO, String)} instead.
     */
    public SalesPortalService(PlaywrightUtils util) {
        this(null, util);
    }


    public String runSalesPortalTest(TestDTO request) {
        if (regionRepository == null) {
            throw new IllegalStateException(
                    "SalesPortalService fue construido sin ComponentRegionRepositoryPort; " +
                            "usa runSalesPortalTest(TestDTO, String url).");
        }
        log.info("Iniciando test para región ID: {}, Usuario: {}",
                request.getRegionId(), request.getUsername());

        ComponentRegion region = regionRepository
                .findById(request.getRegionId(), request.getComponentId())
                .orElseThrow(() -> new RuntimeException("No se encontró la región"));

        String url = util.buildUrl(region);
        return runSalesPortalTest(request, url);
    }

    /**
     * Runs the sales-portal flow against a pre-resolved URL, skipping the DB
     * lookup. Suitable for the programmatic test runner where the target URL
     * comes from {@code config-{env}.properties} (TestConfig).
     */
    public String runSalesPortalTest(TestDTO request, String url) {
        log.info("URL de destino: {}", url);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = util.createBrowser(playwright);
            BrowserContext context = browser.newContext(util.configureContext());
            Page page = context.newPage();

            util.configurePageDefaults(page);

            try {
                performLogin(page, url, request);
                Thread.sleep(1000);
                handleModalDialog(page);
                Thread.sleep(1000);
                enterClerkId(page, request.getClerkId());

                verifyLoginSuccess(page);

                String receipt = processTransaction(page, request);

                log.info("Transacción completada exitosamente para usuario: {}", request.getUsername());
                return util.buildSuccessResponse(receipt);

            } catch (Exception e) {
                throw new RuntimeException("Error durante la ejecución del test: " + e.getMessage(), e);
            } finally {
                util.closeResources(context, browser);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }




    private void performLogin(Page page, String url, TestDTO request) {
        log.info("Realizando login en: {}", url);

        page.navigate(url, new Page.NavigateOptions());

        util.fillFieldWithRetry(page, "input[name=\"userId\"]", request.getUsername(), "Usuario");
        util.fillFieldWithRetry(page, "input[name=\"password\"]", request.getPassword(), "Contraseña");

        util.clickWithRetry(page, "button[type=\"submit\"]", "Botón de login");

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    private void handleModalDialog(Page page) {
        try {
            Locator dialog = page.locator("button[aria-label=\"Close\"], button[aria-label=\"Cerrar\"]");
            dialog.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(util.shortTimeout().toMillis()));

            Locator closeButton = page.locator("button[aria-label=\"Close\"], button[aria-label=\"Cerrar\"]");
            closeButton.click(new Locator.ClickOptions()
                    .setTimeout(util.shortTimeout().toMillis()));


            log.info("Diálogo modal cerrado exitosamente");
        } catch (Exception e) {
            log.info("No se encontró diálogo modal o ya estaba cerrado");
        }
    }

    private void verifyLoginSuccess(Page page) {
        String[] welcomeMessages = {"Bienvenido,", "Welcome,"};

        for (String message : welcomeMessages) {
            try {
                page.getByText(message, new Page.GetByTextOptions().setExact(false))
                        .waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(util.shortTimeout().toMillis()));
                log.info("Login verificado con mensaje: {}", message);
                return;
            } catch (Exception e) {
                // Continuar con el siguiente mensaje
            }
        }

        throw new RuntimeException("No se pudo verificar el login exitoso");
    }


    private String processTransaction(Page page, TestDTO request) throws InterruptedException {

        return switch (request.getProductFlowType()) {
            case TOP_UP -> processTopUp(page, request);
            case PIN -> processPin(page, request);
            case BILL_PAYMENT -> processBillPayment(page, request);
            case CRYPTO -> null;
            case ACTIVATION -> null;
            case PORT_IN -> null;
            case TOOL -> null;
            case AIR_TIME -> null;
        };
    }

    private String processBaseFlow(Page page, TestDTO request) throws InterruptedException {

        ProductFlowType flowType = request.getProductFlowType();
        searchProduct(page, flowType.getPurchaseFlow().getDisplayName());

        selectCarrier(page, request.getCarrier());
        selectCategory(page, flowType.getDisplayName());
        selectProduct(page, request.getProduct(), request.getAmount());
        Thread.sleep(1000);
        confirmButton(page);
        if (request.isPhoneNumberEnabled()){
            enterPhoneNumber(page, request.getPhoneNumber());
            confirmButton(page);
        }

        return getTransactionReceipt(page);
    }

    private String processTopUp(Page page, TestDTO request) throws InterruptedException {
        return processBaseFlow(page, request);
    }
    private String processPin(Page page, TestDTO request) throws InterruptedException {
        return processBaseFlow(page, request);
    }
    private String processBillPayment(Page page, TestDTO request) throws InterruptedException {
        return processBaseFlow(page, request);
    }

    private void searchProduct(Page page, String productType) {
        log.info("Buscando producto: {}", productType);

        String[] searchButtonSelectors = {
                "button[aria-label=\"Open search\"]",
                "button[aria-label=\"Abrir búsqueda\"]",
                "button[title*=\"search\"]",
                "button[title*=\"buscar\"]",
                "[role=\"button\"]:has(svg.search-icon)"
        };

        util.clickWithSelectorOptions(page, searchButtonSelectors, "Botón de búsqueda");

        String[] searchInputSelectors = {
                "input[placeholder*='Buscar servicios']",
                "input[placeholder*='Search for services']",
                "input[type='search']",
                "input[name*='search']"
        };

        util.fillWithSelectorOptions(page, searchInputSelectors, productType, "Campo de búsqueda");

        String[] searchFlowSelectors = {
                "button:has-text(\"" + productType + "\")",
                "text=" + productType
        };

        util.clickWithSelectorOptions(page, searchFlowSelectors, "Acción de búsqueda");


        page.waitForLoadState(LoadState.NETWORKIDLE);

    }


    private void selectCarrier(Page page, String carrier) {
        log.info("Seleccionando carrier: {}", carrier);

        util.waitForAnyText(page,
                new String[]{"Selecciona el operador", "Select the carrier"},
                "Texto de selección de carrier");

        clickCarrierOrProduct(page, carrier, "Carrier");
    }
    private void selectCategory(Page page, String category) {
        log.info("Seleccionando categoria: {}", category);

        util.waitForAnyText(page,
                new String[]{"Seleccione la categoría del servicio que prefieras.", "Choose the category of service you prefer."},
                "Texto de selección de categoria");

        clickCarrierOrProduct(page, category, "Categoria");
    }

    private void selectProduct(Page page, String product, String amount) {
        log.info("Seleccionando producto: {}", product);

        util.waitForAnyText(page,
                new String[]{"Seleccione la categoría del servicio que prefieras.", "Choose the category of service you prefer."},
                "Texto de selección de producto");

        clickCarrierOrProduct(page, product, "Producto");

        String[] amountSelectors = {
                "div#input-variable-amount",
                "input[inputmode='decimal']",
        };

        if (util.isElementVisible(page, amountSelectors)) {
            util.fillWithSelectorOptions(page, amountSelectors, amount, "Campo de monto");
        } else {
            log.info("Producto con precio fijo, se omite el monto");
        }
    }

    private void clickCarrierOrProduct(Page page, String item, String type) {
        String[] selectors = {
                "button:has-text(\"" + item + "\")",
                "text=\"" + item + "\"",
                "button[data-pr-tooltip=\"" + item + "\"]",
                "button[title=\"" + item + "\"]",

        };

        util.clickWithSelectorOptions(page, selectors, type + ": " + item);

    }


    private void enterPhoneNumber(Page page, String phoneNumber) {
        log.info("Ingresando numero de telefono: {}", phoneNumber);

        util.waitForAnyText(page,
                new String[]{"Mobile Number", "Número Móvil"},
                "Número de teléfono");

        String[] phoneSelectors = {
                "input[id=phone]",
                "input[inputmode=numeric]",
                "input[maxlength=14]",
        };

        util.fillWithSelectorOptions(page, phoneSelectors, phoneNumber, "Campo número de telefono");

        String[] phoneConfirmSelectors = {
                "input[id=\"confirm-phone\"]",
                "input[inputmode=\"numeric\"]",
        };

        util.fillWithSelectorOptions(page, phoneConfirmSelectors, phoneNumber, "Campo número de telefono");

    }

    private void enterClerkId(Page page, String clerkId) {
        String[] clerkSelectors = {
                "input[inputmode='numeric'][placeholder='0000']",
                "input[inputmode='numeric']",
                "input[placeholder='0000']",
        };

        if (util.isElementVisible(page, clerkSelectors)) {
            util.fillWithSelectorOptions(page, clerkSelectors, clerkId, "Campo de monto");
            confirmButton(page);
        } else {
            log.info("ClerkId no encontrado, se omite el campo");
        }
    }

    private void confirmButton(Page page) {
        log.info("Confirmando transacción");

        String[] confirmSelectors = {
                "button:has-text('Confirm')",
                "button:has-text('Confirmar')",
                "button:text('Confirm')",
                "button:text('Confirmar')",
        };

        util.clickWithSelectorOptions(page, confirmSelectors, "Botón de confirmar transacción");

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    private String getTransactionReceipt(Page page) {
        log.info("Obteniendo recibo de transacción");

        Locator receiptElement = page.locator("#TransactionDetails");
        receiptElement.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(util.longTimeout().toMillis()));

        String receipt = "";
        int attempts = 0;
        while (attempts < 10 && (receipt == null || receipt.trim().length() < 10)) {
            receipt = receiptElement.innerText();
            if (receipt.trim().length() < 10) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                attempts++;
            }
        }

        if (receipt == null || receipt.trim().length() < 10) {
            throw new RuntimeException("Recibo no contiene información suficiente");
        }

        log.info("Recibo obtenido exitosamente");
        return receipt;
    }

}