package com.example.demo.e2e.flows;

import com.example.demo.config.TestConfig;
import com.microsoft.playwright.Locator;
import lombok.extern.slf4j.Slf4j;

/**
 * Flujo BILL_PAYMENT (pago de factura). Esqueleto.
 *
 * <p>TODO: confirmar contra el formulario real los nombres de los inputs.
 * Suposición: {@code accountNumber} + {@code accountNumberConfirm} + {@code amount}.
 */
@Slf4j
final class BillPaymentPurchaseFlow extends AbstractPurchaseFlow {

    @Override
    public void fillPurchaseForm(Locator modal, String clerkId) {
        String account = TestConfig.getOrDefault("pcterminal.bill.accountNumber", "");
        String amount = TestConfig.getOrDefault("pcterminal.bill.amount", "");

        fillByName(modal, "accountNumber", account);
        fillByName(modal, "accountNumberConfirm", account);
        fillByName(modal, "amount", amount);

        fillClerkIdIfEnabled(modal, clerkId);
    }
}
