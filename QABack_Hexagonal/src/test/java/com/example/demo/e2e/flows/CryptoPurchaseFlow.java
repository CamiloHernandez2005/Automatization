package com.example.demo.e2e.flows;

import com.example.demo.config.TestConfig;
import com.microsoft.playwright.Locator;
import lombok.extern.slf4j.Slf4j;

/**
 * Flujo CRYPTO. Esqueleto.
 *
 * <p>TODO: confirmar el {@code name} real del campo del wallet — posibles
 * candidatos: {@code walletAddress}, {@code address}, {@code wallet}.
 */
@Slf4j
final class CryptoPurchaseFlow extends AbstractPurchaseFlow {

    @Override
    public void fillPurchaseForm(Locator modal, String clerkId) {
        String wallet = TestConfig.getOrDefault("pcterminal.crypto.wallet", "");
        String amount = TestConfig.getOrDefault("pcterminal.crypto.amount", "");

        fillByName(modal, "walletAddress", wallet);
        fillByName(modal, "amount", amount);

        fillClerkIdIfEnabled(modal, clerkId);
    }
}
