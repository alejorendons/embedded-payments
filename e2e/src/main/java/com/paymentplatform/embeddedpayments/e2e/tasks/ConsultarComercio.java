package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.MerchantEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

/**
 * Task de Screenplay: consulta los datos de un comercio
 * mediante GET /api/v1/merchants/{id}.
 */
public class ConsultarComercio implements Task {

    private final String merchantIdOverride;
    private final boolean conAuth;

    public ConsultarComercio(String merchantIdOverride, boolean conAuth) {
        this.merchantIdOverride = merchantIdOverride;
        this.conAuth = conAuth;
    }

    /** Consulta el comercio almacenado en memoria del actor, con JWT. */
    public static ConsultarComercio elComercioActual() {
        return Tasks.instrumented(ConsultarComercio.class, null, true);
    }

    /** Consulta un comercio con un ID específico, con JWT. */
    public static ConsultarComercio conId(String merchantId) {
        return Tasks.instrumented(ConsultarComercio.class, merchantId, true);
    }

    /** Consulta el comercio actual sin JWT (sin permisos). */
    public static ConsultarComercio sinAutenticacion() {
        return Tasks.instrumented(ConsultarComercio.class, null, false);
    }

    @Override
    @Step("{0} consulta el comercio por ID")
    public <T extends Actor> void performAs(T actor) {
        String id = merchantIdOverride != null ? merchantIdOverride : actor.recall("merchantId");

        var request = SerenityRest.given()
                .contentType("application/json");

        if (conAuth) {
            String jwt = actor.recall("jwtToken");
            if (jwt != null) {
                request = request.header("Authorization", "Bearer " + jwt);
            }
        }

        request.when().get(MerchantEndpoints.OBTENER.replace("{id}", id));
    }
}
