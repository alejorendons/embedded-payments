package com.paymentplatform.embeddedpayments.e2e.endpoints;

/**
 * Constantes de rutas para los endpoints de gestión de administradores,
 * ledger financiero y auditoría.
 */
public final class AdminEndpoints {

    private AdminEndpoints() {
    }

    /** PUT – Actualizar datos personales del administrador autenticado (requiere JWT). */
    public static final String ACTUALIZAR_PERFIL = "/api/v1/auth/me";

    /** POST – Cambiar contraseña del administrador autenticado (requiere JWT). */
    public static final String CAMBIAR_CONTRASENA = "/api/v1/auth/change-password";

    /** GET – Consultar balance consolidado (ledger) del sistema (requiere ADMIN JWT). */
    public static final String LEDGER = "/api/v1/ledger";

    /** GET – Consultar registros de auditoría de cambios de estado (requiere ADMIN JWT). */
    public static final String AUDITORIA = "/api/v1/audit";
}
