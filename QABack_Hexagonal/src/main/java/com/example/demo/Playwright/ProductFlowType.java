package com.example.demo.Playwright;

import lombok.Getter;
import java.util.Locale;

@Getter
public enum ProductFlowType {

    TOP_UP("TOP UP", "RECARGA"),
    WIRELESS("WIRELESS", "SERVICIO INALÁMBRICO"),
    BILL_PAYMENT("BILL PAYMENT", "PAGO DE FACTURA"),
    CRYPTO("CRYPTO", "CRIPTOMONEDAS"),
    ACTIVATION("ACTIVATION", "ACTIVACIÓN"),
    PORT_IN("PORT IN", "PORTABILIDAD"),
    TOOL("TOOL", "HERRAMIENTA"),
    AIR_TIME("AIR TIME", "TIEMPO AL AIRE");

    private final String englishName;
    private final String spanishName;

    ProductFlowType(String englishName, String spanishName) {
        this.englishName = englishName;
        this.spanishName = spanishName;
    }

    public String getDisplayName(Locale locale) {
        if (locale != null && locale.getLanguage().equals("es")) {
            return spanishName;
        }
        return englishName;
    }

    public String getDisplayName() {
        return englishName;
    }
}