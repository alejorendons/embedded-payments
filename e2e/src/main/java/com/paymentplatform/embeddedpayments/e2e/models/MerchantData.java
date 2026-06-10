package com.paymentplatform.embeddedpayments.e2e.models;

import lombok.Builder;
import lombok.Data;

/**
 * Modelo de datos de prueba para el recurso Comercio.
 * Agrupa los campos necesarios para construir los payloads de registro
 * y actualización de contacto durante los escenarios E2E.
 */
@Data
@Builder
public class MerchantData {

    private String nombre;
    private String email;
    private String contactName;
    private String contactEmail;
    private String merchantId;
    private String apiKey;
    private String jwtToken;
}
