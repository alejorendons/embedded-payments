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
 * Task de Screenplay: desactiva un comercio mediante PATCH /api/v1/merchants/{id}/deactivate.
 * Requiere JWT con ROLE_ADMIN.
 */
public class DesactivarComercio implements Task {

    private final String merchantIdOverride;
    private final String razon;

    public DesactivarComercio(String merchantIdOverride, String razon) {
        this.merchantIdOverride = merchantIdOverride;
        this.razon = razon;
    }

    /** Desactiva el comercio almacenado en la memoria del actor. */
    public static DesactivarComercio elComercioActual() {
        return Tasks.instrumented(DesactivarComercio.class, null, "Desactivación por administrador E2E");
    }

    @Override
    @Step("{0} desactiva el comercio")
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
                .patch(MerchantEndpoints.DESACTIVAR.replace("{id}", id));
    }
}
