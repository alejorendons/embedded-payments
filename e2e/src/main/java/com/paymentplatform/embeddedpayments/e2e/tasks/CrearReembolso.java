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
 * Task de Screenplay: crea un reembolso sobre una transacción procesada
 * mediante POST /api/v1/refunds.
 * Autenticación: X-API-Key almacenada en la memoria del actor.
 *
 * <p>Si el reembolso se crea exitosamente (HTTP 201), almacena en la memoria:
 * <ul>
 *   <li>{@code refundId} – UUID del reembolso creado.</li>
 * </ul>
 */
public class CrearReembolso implements Task {

    private final String transactionIdOverride;
    private final String amount;
    private final String reason;

    public CrearReembolso(String transactionIdOverride, String amount, String reason) {
        this.transactionIdOverride = transactionIdOverride;
        this.amount = amount;
        this.reason = reason;
    }

    /** Crea un reembolso completo de la transacción almacenada en memoria. */
    public static CrearReembolso deTransaccionActual() {
        return Tasks.instrumented(CrearReembolso.class, null, "100.00", "Reembolso solicitado por el cliente");
    }

    /** Intenta reembolsar una transacción con un ID específico (p.ej. de un pago cancelado). */
    public static CrearReembolso deTransaccionConId(String transactionId) {
        return Tasks.instrumented(CrearReembolso.class, transactionId, "100.00", "Reembolso E2E");
    }

    @Override
    @Step("{0} solicita reembolso de '#amount' por '#reason'")
    public <T extends Actor> void performAs(T actor) {
        String apiKey = actor.recall("apiKey");
        String txId = transactionIdOverride != null
                ? transactionIdOverride
                : actor.recall("transactionId");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("transaction_id", txId);
        body.put("amount", new BigDecimal(amount));
        body.put("reason", reason);

        Response response = SerenityRest.given()
                .header("X-API-Key", apiKey)
                .contentType("application/json")
                .body(body)
                .when()
                .post(PaymentEndpoints.CREAR_REEMBOLSO);

        if (response.statusCode() == 201) {
            String refundId = response.jsonPath().getString("id");
            if (refundId != null) {
                actor.remember("refundId", refundId);
            }
        }
    }
}
