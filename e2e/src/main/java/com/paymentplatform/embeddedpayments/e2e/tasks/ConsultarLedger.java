package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.AdminEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

/**
 * Task de Screenplay: consulta el ledger financiero consolidado
 * mediante GET /api/v1/ledger.
 * Requiere JWT con ROLE_ADMIN.
 * Soporta filtro opcional por comercio específico.
 */
public class ConsultarLedger implements Task {

    private final String merchantIdFiltro;

    public ConsultarLedger(String merchantIdFiltro) {
        this.merchantIdFiltro = merchantIdFiltro;
    }

    /** Consulta el balance consolidado de todos los comercios. */
    public static ConsultarLedger consolidado() {
        return Tasks.instrumented(ConsultarLedger.class, (String) null);
    }

    /** Consulta el ledger filtrado por el comercio almacenado en la memoria del actor. */
    public static ConsultarLedger delMerchantActual() {
        return Tasks.instrumented(ConsultarLedger.class, "use_actor_memory");
    }

    /** Consulta el ledger filtrado por un comercio específico. */
    public static ConsultarLedger porMerchant(String merchantId) {
        return Tasks.instrumented(ConsultarLedger.class, merchantId);
    }

    @Override
    @Step("{0} consulta el ledger financiero")
    public <T extends Actor> void performAs(T actor) {
        String jwt = actor.recall("jwtToken");

        String filtro = "use_actor_memory".equals(merchantIdFiltro)
                ? actor.recall("merchantId")
                : merchantIdFiltro;

        var request = SerenityRest.given()
                .header("Authorization", "Bearer " + jwt)
                .contentType("application/json");

        if (filtro != null) {
            request = request.queryParam("merchantId", filtro);
        }

        request.when().get(AdminEndpoints.LEDGER);
    }
}
