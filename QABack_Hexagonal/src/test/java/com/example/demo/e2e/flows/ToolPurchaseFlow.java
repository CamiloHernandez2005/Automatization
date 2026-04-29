package com.example.demo.e2e.flows;

import com.microsoft.playwright.Locator;
import lombok.extern.slf4j.Slf4j;

/**
 * Flujo TOOL. Esqueleto mínimo.
 *
 * <p>TODO: confirmar si el flujo TOOL requiere algún campo adicional. La
 * suposición actual es que solo necesita el clerkId (si está activo).
 */
@Slf4j
final class ToolPurchaseFlow extends AbstractPurchaseFlow {

    @Override
    public void fillPurchaseForm(Locator modal, String clerkId) {
        fillClerkIdIfEnabled(modal, clerkId);
    }
}
