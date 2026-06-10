package com.paymentplatform.embeddedpayments.e2e.utils;

import java.util.UUID;

/**
 * Fábrica de datos de prueba para garantizar unicidad entre ejecuciones.
 * Usa UUID aleatorio como sufijo para evitar colisiones en la base de datos.
 */
public final class DataFactory {

    private DataFactory() {
    }

    /** Genera un email único con el prefijo indicado. */
    public static String uniqueEmail(String prefix) {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefix + "." + uid + "@e2etest.com";
    }

    /** Genera un nombre de comercio único. */
    public static String uniqueMerchantName(String base) {
        return base + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    /** Genera un UUID aleatorio como string para simular IDs inexistentes. */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }
}
