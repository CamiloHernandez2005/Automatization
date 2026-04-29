package com.example.demo.e2e.flows;

import com.example.demo.config.TestConfig;
import com.microsoft.playwright.Locator;
import lombok.extern.slf4j.Slf4j;

/**
 * Helpers comunes para todas las {@link PurchaseFlowStrategy}: mantiene cortas
 * y consistentes las implementaciones concretas en cómo manejan campos
 * opcionales, valores vacíos y la flag global de clerkId.
 */
@Slf4j
abstract class AbstractPurchaseFlow implements PurchaseFlowStrategy {

    /**
     * Llena {@code field} con {@code value} solo si:
     * <ul>
     *   <li>el campo está presente y visible en el DOM, y</li>
     *   <li>{@code value} no es {@code null} ni está en blanco.</li>
     * </ul>
     * Esto evita borrar valores autocompletados por la app cuando una clave
     * de configuración no fue provista.
     */
    protected void fillIfVisible(Locator field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (field.count() > 0 && field.first().isVisible()) {
            field.first().fill(value);
        }
    }

    /**
     * Atajo: equivale a
     * {@code fillIfVisible(modal.locator("input[name='<name>']"), value)}.
     */
    protected void fillByName(Locator modal, String name, String value) {
        fillIfVisible(modal.locator("input[name=\"" + name + "\"]"), value);
    }

    /**
     * Lee la flag global {@code pcterminal.clerkId.enabled} y, si está activa,
     * rellena {@code input[name="clerkId"]} con el valor recibido.
     */
    protected void fillClerkIdIfEnabled(Locator modal, String clerkId) {
        boolean enabled = TestConfig.getOrDefault("pcterminal.clerkId.enabled", false);
        if (!enabled) {
            log.debug("clerkId.enabled=false → se omite el campo clerkId");
            return;
        }
        Locator field = modal.locator("input[name=\"clerkId\"]");
        if (field.count() > 0 && field.first().isVisible()) {
            field.first().focus();
            fillIfVisible(field, clerkId);
        } else {
            log.warn("clerkId.enabled=true pero el campo clerkId no está visible en el modal");
        }
    }
}
