package com.example.demo.e2e.flows;

import com.example.demo.config.TestConfig;
import com.microsoft.playwright.Locator;
import lombok.extern.slf4j.Slf4j;

/**
 * Flujo PORT_IN (portabilidad). Esqueleto.
 *
 * <p>TODO: confirmar selectores. {@code currentCarrier} muy probablemente sea
 * un {@code <select>} y haya que cambiar a
 * {@code modal.locator("select[name='currentCarrier']").selectOption(carrier)}
 * en vez de un fill por input.
 */
@Slf4j
final class PortInPurchaseFlow extends AbstractPurchaseFlow {

    @Override
    public void fillPurchaseForm(Locator modal, String clerkId) {
        String number = TestConfig.getOrDefault("pcterminal.portin.number", "");
        String carrier = TestConfig.getOrDefault("pcterminal.portin.carrier", "");
        String account = TestConfig.getOrDefault("pcterminal.portin.account", "");
        String pin = TestConfig.getOrDefault("pcterminal.portin.pin", "");

        fillByName(modal, "currentNumber", number);
        // TODO: si currentCarrier es <select>, reemplazar por:
        // modal.locator("select[name=\"currentCarrier\"]").selectOption(carrier);
        fillByName(modal, "currentCarrier", carrier);
        fillByName(modal, "accountNumber", account);
        fillByName(modal, "transferPin", pin);

        fillClerkIdIfEnabled(modal, clerkId);
    }
}
