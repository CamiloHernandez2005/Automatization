package com.example.demo.e2e.flows;

import com.example.demo.config.TestConfig;
import com.microsoft.playwright.Locator;
import lombok.extern.slf4j.Slf4j;

/**
 * Flujo ACTIVATION. Esqueleto.
 *
 * <p>TODO: confirmar selectores. Suposición: ICCID + IMEI son los típicos
 * para activar una SIM.
 */
@Slf4j
final class ActivationPurchaseFlow extends AbstractPurchaseFlow {

    @Override
    public void fillPurchaseForm(Locator modal, String clerkId) {
        String iccid = TestConfig.getOrDefault("pcterminal.activation.iccid", "");
        String imei = TestConfig.getOrDefault("pcterminal.activation.imei", "");

        fillByName(modal, "iccid", iccid);
        fillByName(modal, "imei", imei);

        fillClerkIdIfEnabled(modal, clerkId);
    }
}
