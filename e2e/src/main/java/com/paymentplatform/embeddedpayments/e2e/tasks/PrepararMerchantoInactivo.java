package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.MerchantEndpoints;
import com.paymentplatform.embeddedpayments.e2e.exceptions.E2EException;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task de setup: registra un comercio vía POST /api/v1/merchants (endpoint público),
 * que queda en estado INACTIVE, y almacena su apiKey y merchantId en la memoria del actor.
 *
 * <p>Útil para escenarios que validan el bloqueo de operaciones en comercios inactivos.
 *
 * <p>Almacena en la memoria del actor:
 * <ul>
 *   <li>{@code apiKey}     – API key (epk_...) del comercio inactivo.</li>
 *   <li>{@code merchantId} – UUID del comercio creado.</li>
 * </ul>
 */
public class PrepararMerchantoInactivo implements Task {

    private final String nombre;
    private final String email;

    public PrepararMerchantoInactivo(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    public static PrepararMerchantoInactivo conNombreYEmail(String nombre, String email) {
        return Tasks.instrumented(PrepararMerchantoInactivo.class, nombre, email);
    }

    @Override
    @Step("{0} registra un comercio INACTIVO con email '#email' y recuerda su apiKey")
    public <T extends Actor> void performAs(T actor) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("name", nombre);
        body.put("email", email);

        Response response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .post(MerchantEndpoints.REGISTRAR);

        if (response.statusCode() != 201) {
            throw new E2EException(
                    "Registro de comercio inactivo fallido. Status: " + response.statusCode()
                            + " | Body: " + response.asString());
        }

        String merchantId = response.jsonPath().getString("id");
        String apiKey = response.jsonPath().getString("api_key");

        actor.remember("apiKey", apiKey);
        actor.remember("merchantId", merchantId);
    }
}
