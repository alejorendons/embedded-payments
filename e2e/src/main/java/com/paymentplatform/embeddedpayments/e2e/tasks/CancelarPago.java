package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.PaymentEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

/**
 * Task de Screenplay: cancela una intención de pago no procesada
 * mediante PATCH /api/v1/payments/intents/{id}/cancel.
 * Autenticación: X-API-Key almacenada en la memoria del actor.
 */
public class CancelarPago implements Task {

    private final String paymentIntentIdOverride;

    public CancelarPago(String paymentIntentIdOverride) {
        this.paymentIntentIdOverride = paymentIntentIdOverride;
    }

    /** Cancela la intención de pago almacenada en la memoria del actor. */
    public static CancelarPago laIntencionActual() {
        return Tasks.instrumented(CancelarPago.class, (String) null);
    }

    /** Cancela una intención de pago con un ID específico. */
    public static CancelarPago conId(String paymentIntentId) {
        return Tasks.instrumented(CancelarPago.class, paymentIntentId);
    }

    @Override
    @Step("{0} cancela la intención de pago")
    public <T extends Actor> void performAs(T actor) {
        String apiKey = actor.recall("apiKey");
        String intentId = paymentIntentIdOverride != null
                ? paymentIntentIdOverride
                : actor.recall("paymentIntentId");

        SerenityRest.given()
                .header("X-API-Key", apiKey)
                .contentType("application/json")
                .when()
                .patch(PaymentEndpoints.CANCELAR_INTENCION.replace("{id}", intentId));
    }
}
