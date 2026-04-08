package com.example.demo.Playwright;

import com.example.demo.application.componentsRegions.ports.out.ComponentRegionRepositoryPort;
import com.example.demo.domain.ComponentRegion;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class SalesPortalService {
    private final ComponentRegionRepositoryPort regionRepository;
    private final PlaywrightUtils util;

    public SalesPortalService(ComponentRegionRepositoryPort regionRepository, PlaywrightUtils util) {
        this.regionRepository = regionRepository;
        this.util = util;
    }


    public String runSalesPortalTest(TestDTO request) {
        log.info("Iniciando test para región ID: {}, Usuario: {}",
                request.getRegionId(), request.getUsername());

        ComponentRegion region = regionRepository
                .findById(request.getRegionId(), request.getComponentId())
                .orElseThrow(() -> new RuntimeException("No se encontró la región"));

        String url = util.buildUrl(region);

        log.debug("URL de destino: {}", url);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = util.createBrowser(playwright);
            BrowserContext context = browser.newContext(util.configureContext());
            Page page = context.newPage();

            util.configurePageDefaults(page);

            try {
                performLogin(page, url, request);
                Thread.sleep(1000);
                handleModalDialog(page);
                Thread.sleep(2000);

                enterClerkId(page, request.getClerkId());
                confirmButton(page);

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
        log.debug("Realizando login en: {}", url);

        page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.NETWORKIDLE));

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
                    .setTimeout(util.SHORT_TIMEOUT.toMillis()));

            Locator closeButton = page.locator("button[aria-label=\"Close\"], button[aria-label=\"Cerrar\"]");
            closeButton.click(new Locator.ClickOptions()
                    .setTimeout(util.DEFAULT_TIMEOUT.toMillis()));


            log.debug("Diálogo modal cerrado exitosamente");
        } catch (Exception e) {
            log.debug("No se encontró diálogo modal o ya estaba cerrado");
        }
    }

    private void verifyLoginSuccess(Page page) {
        String[] welcomeMessages = {"Bienvenido,", "Welcome,"};

        for (String message : welcomeMessages) {
            try {
                page.getByText(message, new Page.GetByTextOptions().setExact(false))
                        .waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(util.DEFAULT_TIMEOUT.toMillis()));
                log.debug("Login verificado con mensaje: {}", message);
                return;
            } catch (Exception e) {
                // Continuar con el siguiente mensaje
            }
        }

        throw new RuntimeException("No se pudo verificar el login exitoso");
    }

    private String processTransaction(Page page, TestDTO request) {
        searchProduct(page, request.getProductFlowType().getDisplayName());

        selectCarrier(page, request.getCarrier());
        selectCategory(page, request.getCategory());
        selectProduct(page, request.getProduct(), request.getAmount());
        confirmButton(page);
        if (request.isPhoneNumberEnabled()){
            enterPhoneNumber(page, request.getPhoneNumber());
            confirmButton(page);
        }
        if (request.isClerkIdEnabled()){
            enterClerkId(page, request.getClerkId());
            confirmButton(page);
        }

        return getTransactionReceipt(page);
    }
//    private String processTransaction(Page page, TestDTO request) {
//
//        return switch (request.getProductFlowType()) {
//            case TOPUP -> processTopUp(page, request);
//            case PIN -> processPin(page, request);
//            case BILL_PAYMENT -> processBillPayment(page, request);
//        };
//    }

//    private String processBaseFlow(
//            Page page,
//            TestDTO request,
//            boolean includePhone,
//    ) {
//
//        searchProduct(page, String.valueOf(request.getProductFlowType()));
//        selectCarrier(page, request.getCarrier());
//        selectProduct(page, request.getProduct(), request.getAmount());
//
//        confirmButton(page);
//
//        if (includePhone) {
//            enterPhoneNumber(page, request.getPhoneNumber());
//            confirmButton(page);
//        }
//
//        enterClerkId(page, request.getClerkId());
//        confirmButton(page);
//
//
//        return getTransactionReceipt(page);
//    }

//    private String processTopUp(Page page, TestDTO request) {
//        return processBaseFlow(page, request, true, true);
//    }

    private void searchProduct(Page page, String productType) {
        log.debug("Buscando producto: {}", productType);

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

        util.clickWithRetry(page, "button[tabindex=\"0\"], button[type=\"submit\"]", "Acción de búsqueda");

        page.waitForLoadState(LoadState.NETWORKIDLE);

    }


    private void selectCarrier(Page page, String carrier) {
        log.debug("Seleccionando carrier: {}", carrier);

        util.waitForAnyText(page,
                new String[]{"Selecciona el operador", "Select the carrier"},
                "Texto de selección de carrier");

        clickCarrierOrProduct(page, carrier, "Carrier");
    }
    private void selectCategory(Page page, String category) {
        log.debug("Seleccionando categoria: {}", category);

        util.waitForAnyText(page,
                new String[]{"Seleccione la categoría del servicio que prefieras.", "Choose the category of service you prefer."},
                "Texto de selección de categoria");

        clickCarrierOrProduct(page, category, "Categoria");
    }

    private void selectProduct(Page page, String product, String amount) {
        log.debug("Seleccionando producto: {}", product);

        util.waitForAnyText(page,
                new String[]{"Seleccione la categoría del servicio que prefieras.", "Choose the category of service you prefer."},
                "Texto de selección de producto");

        clickCarrierOrProduct(page, product, "Producto");

        String[] amountSelectors = {
                "div[id=\"input-variable-amount\"]",
                "input[type=\"text\"]",
                "input[inputmode*=\"decimal\"]",
        };
        boolean filled = util.tryFillAmount(
                page,
                amountSelectors,
                amount,
                "Campo de monto de la transacción"
        );

        if (!filled) {
            log.debug("Producto sin rango de precio, se omite el monto");
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
        log.debug("Ingresando numero de telefono: {}", phoneNumber);

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
        log.debug("Ingresando ID de empleado: {}", clerkId);

//        util.waitForAnyText(page,
//                new String[]{"Clerk ID", "ID del empleado"},
//                "Texto de ID de Empleado");

        String[] passwordSelectors = {
                "input[inputmode=\"numeric\"]",
                "input[type=\"text\"]",
                "input[name*=\"clerk\"]",
                "input[name*=\"employee\"]",
                "input[placeholder*=\"ID\"]"
        };

        util.fillWithSelectorOptions(page, passwordSelectors, clerkId, "Campo de ID de empleado");
    }

    private void confirmButton(Page page) {
        log.debug("Confirmando transacción");

        String[] confirmSelectors = {
                "button:has-text(\"Confirm\")",
                "button:has-text(\"Confirmar\")",
                "button:text('Confirm')",
                "button:text('Confirmar')",
        };

        util.clickWithSelectorOptions(page, confirmSelectors, "Botón de confirmar transacción");

        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    private String getTransactionReceipt(Page page) {
        log.debug("Obteniendo recibo de transacción");

        Locator receiptElement = page.locator("#TransactionDetails");
        receiptElement.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(util.LONG_TIMEOUT.toMillis()));

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

        log.debug("Recibo obtenido exitosamente");
        return receipt;
    }

}