package com.example.demo.e2e.flows;

import com.example.demo.config.TestConfig;
import com.microsoft.playwright.Locator;
import lombok.extern.slf4j.Slf4j;

/**
 * Flujo PIN. Esqueleto: suposición de campos según el dominio.
 *
 * <p>TODO: confirmar contra el formulario real de PC Terminal qué campos
 * pide este flujo. La suposición actual es solo {@code amount} + clerkId.
 */
@Slf4j
final class PinPurchaseFlow extends AbstractPurchaseFlow {

    @Override
    public void fillPurchaseForm(Locator modal, String clerkId) {
        String amount = TestConfig.getOrDefault("pcterminal.pin.amount", "");

        fillByName(modal, "amount", amount);

        fillClerkIdIfEnabled(modal, clerkId);
    }
}
