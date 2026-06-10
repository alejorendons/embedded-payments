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
 * Task de Screenplay: activa un comercio mediante PATCH /api/v1/merchants/{id}/activate.
 * Requiere JWT con ROLE_ADMIN.
 */
public class ActivarComercio implements Task {

    private final String merchantIdOverride;
    private final String razon;

    public ActivarComercio(String merchantIdOverride, String razon) {
        this.merchantIdOverride = merchantIdOverride;
        this.razon = razon;
    }

    /** Activa el comercio almacenado en la memoria del actor. */
    public static ActivarComercio elComercioActual() {
        return Tasks.instrumented(ActivarComercio.class, null, "Activación por administrador E2E");
    }

    /** Intenta activar un comercio con un ID específico (puede ser inexistente). */
    public static ActivarComercio conId(String merchantId) {
        return Tasks.instrumented(ActivarComercio.class, merchantId, "Activación por administrador E2E");
    }

    @Override
    @Step("{0} activa el comercio")
    public <T extends Actor> void performAs(T actor) {
        String jwt = actor.recall("jwtToken");
        String id = merchantIdOverride != null ? merchantIdOverride : actor.recall("merchantId");

        Map<String, String> body = new LinkedHashMap<>();
        body.put("reason", razon);

        SerenityRest.given()
                .header("Authorization", "Bearer " + jwt)
                .contentType("application/json")
                .body(body)
                .when()
                .patch(MerchantEndpoints.ACTIVAR.replace("{id}", id));
    }
}
