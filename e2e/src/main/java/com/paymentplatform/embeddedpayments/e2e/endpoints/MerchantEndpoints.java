package com.paymentplatform.embeddedpayments.e2e.endpoints;

/**
 * Constantes con las rutas de los endpoints de la API de Comercios.
 * Equivalente al rol de Page Objects en pruebas de UI: centraliza
 * las URLs para que los Tasks no dependan de literales dispersos.
 */
public final class MerchantEndpoints {

    private MerchantEndpoints() {
    }

    /** POST – Registrar un nuevo comercio (público). */
    public static final String REGISTRAR = "/api/v1/merchants";

    /** PUT – Actualizar información de contacto del comercio (requiere ADMIN o MERCHANT). */
    public static final String ACTUALIZAR_CONTACTO = "/api/v1/merchants/{id}/contact";

    /** GET – Obtener detalles de un comercio por ID. */
    public static final String OBTENER = "/api/v1/merchants/{id}";

    /** PATCH – Activar un comercio (requiere ADMIN). */
    public static final String ACTIVAR = "/api/v1/merchants/{id}/activate";

    /** PATCH – Desactivar un comercio (requiere ADMIN). */
    public static final String DESACTIVAR = "/api/v1/merchants/{id}/deactivate";

    /** PUT – Registrar cuenta bancaria de un comercio (requiere ADMIN o MERCHANT, comercio ACTIVE). */
    public static final String REGISTRAR_CUENTA_BANCARIA = "/api/v1/merchants/{id}/bank-account";
}
