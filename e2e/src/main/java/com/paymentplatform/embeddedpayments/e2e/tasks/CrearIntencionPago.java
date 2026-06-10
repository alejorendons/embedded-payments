package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.PaymentEndpoints;
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
 * Task de Screenplay: crea una intención de pago
 * mediante POST /api/v1/payments/intents.
 * Autenticación: X-API-Key (epk_...) almacenada en la memoria del actor.
 *
 * <p>Si el pago se crea exitosamente (HTTP 201), almacena en la memoria del actor:
 * <ul>
 *   <li>{@code paymentIntentId} – UUID de la intención de pago creada.</li>
 * </ul>
 */
public class CrearIntencionPago implements Task {

    private final String amount;
    private final String currency;

    public CrearIntencionPago(String amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    /** Crea una intención de pago con monto y moneda válidos. */
    public static CrearIntencionPago conMontoValido() {
        return Tasks.instrumented(CrearIntencionPago.class, "100.00", "USD");
    }

    /** Intenta crear una intención de pago con monto cero (inválido). */
    public static CrearIntencionPago conMontoCero() {
        return Tasks.instrumented(CrearIntencionPago.class, "0.00", "USD");
    }

    /** Intenta crear una intención de pago con monto negativo (inválido). */
    public static CrearIntencionPago conMontoNegativo() {
        return Tasks.instrumented(CrearIntencionPago.class, "-50.00", "USD");
    }

    @Override
    @Step("{0} crea una intención de pago por '#amount' '#currency'")
    public <T extends Actor> void performAs(T actor) {
        String apiKey = actor.recall("apiKey");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", new BigDecimal(amount));
        body.put("currency", currency);

        Response response = SerenityRest.given()
                .header("X-API-Key", apiKey)
                .contentType("application/json")
                .body(body)
                .when()
                .post(PaymentEndpoints.CREAR_INTENCION);

        if (response.statusCode() == 201) {
            String paymentIntentId = response.jsonPath().getString("id");
            if (paymentIntentId != null) {
                actor.remember("paymentIntentId", paymentIntentId);
            }
        }
    }
}
