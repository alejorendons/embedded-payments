package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.MerchantEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task de Screenplay: actualiza la información de contacto de un comercio
 * mediante PUT /api/v1/merchants/{id}/contact.
 *
 * Uso autenticado (usa merchantId y jwtToken de la memoria del actor):
 * <pre>
 *   actor.attemptsTo(ActualizarContacto.con("Juan", "juan@test.com"));
 * </pre>
 *
 * Uso sin autenticación (para CA-05):
 * <pre>
 *   actor.attemptsTo(ActualizarContacto.sinAutenticacion("Juan", "juan@test.com", merchantId));
 * </pre>
 *
 * Uso con ID de comercio arbitrario (para CA-04 – comercio inexistente):
 * <pre>
 *   actor.attemptsTo(ActualizarContacto.conMerchantId("Juan", "juan@test.com", randomId));
 * </pre>
 */
public class ActualizarContacto implements Task {

    private final String contactName;
    private final String contactEmail;
    private final String merchantIdOverride;
    private final Boolean incluirToken;

    public ActualizarContacto(String contactName, String contactEmail,
                               String merchantIdOverride, Boolean incluirToken) {
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.merchantIdOverride = merchantIdOverride;
        this.incluirToken = incluirToken;
    }

    /** Actualización autenticada; toma merchantId y JWT de la memoria del actor. */
    public static ActualizarContacto con(String nombre, String email) {
        return Tasks.instrumented(ActualizarContacto.class, nombre, email, null, true);
    }

    /** Actualización autenticada con ID de comercio específico (p.ej. ID inexistente). */
    public static ActualizarContacto conMerchantId(String nombre, String email, String merchantId) {
        return Tasks.instrumented(ActualizarContacto.class, nombre, email, merchantId, true);
    }

    /** Actualización sin token de autenticación (para CA-05). */
    public static ActualizarContacto sinAutenticacion(String nombre, String email, String merchantId) {
        return Tasks.instrumented(ActualizarContacto.class, nombre, email, merchantId, false);
    }

    @Override
    @Step("{0} actualiza el contacto del comercio: nombre='#contactName', email='#contactEmail'")
    public <T extends Actor> void performAs(T actor) {
        String id = merchantIdOverride != null ? merchantIdOverride : actor.recall("merchantId");

        Map<String, String> body = new LinkedHashMap<>();
        body.put("contact_name", contactName);
        body.put("contact_email", contactEmail);

        var request = SerenityRest.given()
                .contentType("application/json")
                .body(body);

        if (incluirToken) {
            String jwt = actor.recall("jwtToken");
            if (jwt != null) {
                request = request.header("Authorization", "Bearer " + jwt);
            }
        }

        request.when().put(MerchantEndpoints.ACTUALIZAR_CONTACTO.replace("{id}", id));
    }
}
