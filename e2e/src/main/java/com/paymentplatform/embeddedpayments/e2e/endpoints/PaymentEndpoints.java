package com.paymentplatform.embeddedpayments.e2e.endpoints;

/**
 * Constantes de rutas para los endpoints de Pagos, Transacciones y Reembolsos.
 * Autenticación: X-API-Key (formato epk_<id>_<secret>) para todos los endpoints.
 */
public final class PaymentEndpoints {

    private PaymentEndpoints() {
    }

    /** POST – Crear intención de pago (requiere API KEY, comercio ACTIVE). */
    public static final String CREAR_INTENCION = "/api/v1/payments/intents";

    /** GET – Consultar estado de intención de pago por ID (requiere API KEY). */
    public static final String OBTENER_INTENCION = "/api/v1/payments/intents/{id}";

    /** PATCH – Cancelar intención de pago no procesada (requiere API KEY). */
    public static final String CANCELAR_INTENCION = "/api/v1/payments/intents/{id}/cancel";

    /** GET – Historial de intenciones de pago del comercio (requiere API KEY). */
    public static final String HISTORIAL_INTENCIONES = "/api/v1/payments/intents";

    /** POST – Crear/autorizar una transacción para una intención de pago (requiere API KEY). */
    public static final String CREAR_TRANSACCION = "/api/v1/transactions";

    /** GET – Listar transacciones (requiere JWT con rol ADMIN). */
    public static final String LISTAR_TRANSACCIONES = "/api/v1/transactions";

    /** POST – Crear un reembolso sobre una transacción procesada (requiere API KEY). */
    public static final String CREAR_REEMBOLSO = "/api/v1/refunds";
}
