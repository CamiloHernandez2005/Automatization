package com.example.demo.e2e.flows;

import com.example.demo.config.TestConfig;
import com.microsoft.playwright.Locator;
import lombok.extern.slf4j.Slf4j;

/**
 * Flujo TOP_UP (recarga). Llena teléfono + confirmación + monto + clerkId.
 *
 * <p>Mantiene los nombres de claves originales sin prefijo
 * ({@code pcterminal.phoneNumber}, {@code pcterminal.amount}) por
 * retrocompatibilidad con el {@code .env} previo al refactor a Strategy.
 */
@Slf4j
final class TopUpPurchaseFlow extends AbstractPurchaseFlow {

    @Override
    public void fillPurchaseForm(Locator modal, String clerkId) {
        boolean phoneEnabled = TestConfig.getOrDefault("pcterminal.phoneNumber.enabled", false);
        String phoneNumber = TestConfig.getOrDefault("pcterminal.phoneNumber", "");
        String amount = TestConfig.getOrDefault("pcterminal.amount", "");

        if (phoneEnabled) {
            fillByName(modal, "phoneNumber", phoneNumber);
            fillByName(modal, "phoneNumberConfirm", phoneNumber);
            fillByName(modal, "amount", amount);
        } else {
            log.debug("phoneNumber.enabled=false → se omiten teléfono y monto");
        }

        fillClerkIdIfEnabled(modal, clerkId);
    }
}
