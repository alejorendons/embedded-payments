package com.paymentplatform.embeddedpayments.e2e.endpoints;

/**
 * Constantes con las rutas de los endpoints de Autenticación.
 * Centraliza las URLs para que los Tasks de autenticación no dependan
 * de literales dispersos en los step definitions.
 */
public final class AuthEndpoints {

    private AuthEndpoints() {
    }

    /** POST – Registrar usuario (puede ser ADMIN o USER). */
    public static final String REGISTRAR = "/api/v1/auth/register";

    /** POST – Iniciar sesión y obtener JWT. */
    public static final String LOGIN = "/api/v1/auth/login";
}
