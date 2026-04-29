package com.example.demo.e2e.flows;

import com.microsoft.playwright.Locator;

/**
 * Estrategia para llenar los campos del modal de compra de PC Terminal según
 * el {@link com.example.demo.Playwright.ProductFlowType} seleccionado por el
 * test.
 *
 * <p>Las estrategias <strong>no</strong> hacen login, búsqueda, ni clickean
 * el botón de Purchase — esa lógica es común a todos los flujos y vive en el
 * helper {@code ejecutarFlujoCompra} del test. Cada estrategia se limita a
 * rellenar los campos específicos de su flujo dentro del modal abierto.
 *
 * <h2>Cómo agregar un nuevo flujo</h2>
 * <ol>
 *   <li>Crear una clase nueva que extienda {@link AbstractPurchaseFlow} y
 *       sobreescriba {@link #fillPurchaseForm(Locator, String)}.</li>
 *   <li>Registrar la nueva estrategia en el bloque {@code static} de
 *       {@link PurchaseFlowRegistry}.</li>
 *   <li>Agregar las claves de configuración correspondientes a los archivos
 *       {@code config-{env}.properties} usando el prefijo
 *       {@code pcterminal.<flow>.<campo>}.</li>
 * </ol>
 */
public interface PurchaseFlowStrategy {

    /**
     * Rellena los campos del modal de compra para el flujo correspondiente.
     *
     * @param modal locator de {@code .modal-body:visible}, ya esperado a
     *              estar visible por el caller.
     * @param clerkId valor a usar para el campo {@code clerkId} si la flag
     *                global {@code pcterminal.clerkId.enabled=true}. Puede ser
     *                un valor inválido cuando el test es negativo.
     */
    void fillPurchaseForm(Locator modal, String clerkId);
}
