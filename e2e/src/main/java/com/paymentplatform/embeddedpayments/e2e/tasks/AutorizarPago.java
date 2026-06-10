package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.PaymentEndpoints;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task de Screenplay: autoriza/procesa un pago creando una transacción
 * mediante POST /api/v1/transactions.
 * Autenticación: X-API-Key (epk_...) almacenada en la memoria del actor.
 *
 * <p>Si la transacción se crea exitosamente (HTTP 201), almacena en la memoria:
 * <ul>
 *   <li>{@code transactionId} – UUID de la transacción creada.</li>
 * </ul>
 */
public class AutorizarPago implements Task {

    private final String paymentIntentIdOverride;
    private final String amount;

    public AutorizarPago(String paymentIntentIdOverride, String amount) {
        this.paymentIntentIdOverride = paymentIntentIdOverride;
        this.amount = amount;
    }

    /** Autoriza la intención de pago almacenada en la memoria del actor. */
    public static AutorizarPago laIntencionActual() {
        return Tasks.instrumented(AutorizarPago.class, null, "100.00");
    }

    /** Intenta autorizar con un ID de intención de pago inexistente. */
    public static AutorizarPago conIdInexistente() {
        return Tasks.instrumented(AutorizarPago.class, DataFactory.randomUUID(), "100.00");
    }

    /** Intenta autorizar con monto inconsistente con la intención. */
    public static AutorizarPago conMontoInconsistente() {
        return Tasks.instrumented(AutorizarPago.class, null, "999999.99");
    }

    @Override
    @Step("{0} autoriza el pago (crea transacción) por '#amount'")
    public <T extends Actor> void performAs(T actor) {
        String apiKey = actor.recall("apiKey");
        String intentId = paymentIntentIdOverride != null
                ? paymentIntentIdOverride
                : actor.recall("paymentIntentId");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("payment_intent_id", intentId);
        body.put("amount", new BigDecimal(amount));

        Response response = SerenityRest.given()
                .header("X-API-Key", apiKey)
                .contentType("application/json")
                .body(body)
                .when()
                .post(PaymentEndpoints.CREAR_TRANSACCION);

        if (response.statusCode() == 201) {
            String transactionId = response.jsonPath().getString("id");
            if (transactionId != null) {
                actor.remember("transactionId", transactionId);
            }
        }
    }
}
