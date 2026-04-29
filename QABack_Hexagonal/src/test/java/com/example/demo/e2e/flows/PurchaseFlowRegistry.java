package com.example.demo.e2e.flows;

import com.example.demo.Playwright.ProductFlowType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registro estático de las {@link PurchaseFlowStrategy} disponibles, indexadas
 * por {@link ProductFlowType}. Reemplaza al {@code switch} sobre el enum: la
 * decisión de qué estrategia ejecutar es una lookup en un {@link EnumMap}.
 *
 * <h2>Cómo agregar un flujo nuevo</h2>
 * <ol>
 *   <li>Crear la clase nueva extendiendo {@link AbstractPurchaseFlow}.</li>
 *   <li>Agregar una entrada
 *       {@code map.put(ProductFlowType.X, new XPurchaseFlow())} al bloque
 *       {@code static} debajo.</li>
 * </ol>
 *
 * <p>Si {@link #get(ProductFlowType)} se llama con un valor del enum sin
 * estrategia registrada, lanza {@link IllegalStateException} con un mensaje
 * que dice exactamente cuál falta — esto evita que un valor nuevo del enum
 * pase silenciosamente sin estrategia asociada.
 *
 * <p>{@link ProductFlowType#AIR_TIME} <strong>no</strong> está registrado a
 * propósito: es un valor derivado por {@link ProductFlowType#getPurchaseFlow()}
 * y no tiene un formulario propio. Si alguien intenta usarlo desde el test,
 * el registry falla rápido con un error claro.
 */
public final class PurchaseFlowRegistry {

    private static final Map<ProductFlowType, PurchaseFlowStrategy> STRATEGIES;

    static {
        EnumMap<ProductFlowType, PurchaseFlowStrategy> map = new EnumMap<>(ProductFlowType.class);
        map.put(ProductFlowType.TOP_UP, new TopUpPurchaseFlow());
        map.put(ProductFlowType.PIN, new PinPurchaseFlow());
        map.put(ProductFlowType.BILL_PAYMENT, new BillPaymentPurchaseFlow());
        map.put(ProductFlowType.CRYPTO, new CryptoPurchaseFlow());
        map.put(ProductFlowType.ACTIVATION, new ActivationPurchaseFlow());
        map.put(ProductFlowType.PORT_IN, new PortInPurchaseFlow());
        map.put(ProductFlowType.TOOL, new ToolPurchaseFlow());
        STRATEGIES = Collections.unmodifiableMap(map);
    }

    private PurchaseFlowRegistry() {
        // utility class
    }

    /**
     * Devuelve la estrategia registrada para {@code type}.
     *
     * @throws IllegalStateException si {@code type} no tiene estrategia
     *         registrada (típicamente porque agregaste un valor nuevo al enum
     *         sin actualizar este registro).
     */
    public static PurchaseFlowStrategy get(ProductFlowType type) {
        PurchaseFlowStrategy strategy = STRATEGIES.get(type);
        if (strategy == null) {
            throw new IllegalStateException(
                    "No hay PurchaseFlowStrategy registrada para " + type +
                            ". Registrala en PurchaseFlowRegistry o usá un " +
                            "ProductFlowType soportado: " + STRATEGIES.keySet());
        }
        return strategy;
    }
}
