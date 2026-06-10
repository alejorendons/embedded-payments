package com.paymentplatform.embeddedpayments.e2e.exceptions;

/**
 * Excepción base para errores producidos durante la ejecución de las
 * pruebas E2E (ej: respuestas inesperadas, estado inconsistente del sistema).
 */
public class E2EException extends RuntimeException {

    public E2EException(String message) {
        super(message);
    }

    public E2EException(String message, Throwable cause) {
        super(message, cause);
    }
}
