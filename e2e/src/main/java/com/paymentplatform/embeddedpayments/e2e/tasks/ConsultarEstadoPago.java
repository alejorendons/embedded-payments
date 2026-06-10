package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.PaymentEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

/**
 * Task de Screenplay: consulta el estado de una intención de pago
 * mediante GET /api/v1/payments/intents/{id}.
 * Autenticación: X-API-Key almacenada en la memoria del actor.
 */
public class ConsultarEstadoPago implements Task {

    private final String paymentIntentIdOverride;

    public ConsultarEstadoPago(String paymentIntentIdOverride) {
        this.paymentIntentIdOverride = paymentIntentIdOverride;
    }

    /** Consulta el estado de la intención de pago almacenada en la memoria del actor. */
    public static ConsultarEstadoPago laIntencionActual() {
        return Tasks.instrumented(ConsultarEstadoPago.class, (String) null);
    }

    /** Consulta una intención de pago con un ID específico (puede pertenecer a otro comercio). */
    public static ConsultarEstadoPago conId(String paymentIntentId) {
        return Tasks.instrumented(ConsultarEstadoPago.class, paymentIntentId);
    }

    @Override
    @Step("{0} consulta el estado de la intención de pago")
    public <T extends Actor> void performAs(T actor) {
        String apiKey = actor.recall("apiKey");
        String intentId = paymentIntentIdOverride != null
                ? paymentIntentIdOverride
                : actor.recall("paymentIntentId");

        SerenityRest.given()
                .header("X-API-Key", apiKey)
                .contentType("application/json")
                .when()
                .get(PaymentEndpoints.OBTENER_INTENCION.replace("{id}", intentId));
    }
}
