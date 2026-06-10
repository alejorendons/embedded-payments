package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.PaymentEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

/**
 * Task de Screenplay: consulta las transacciones del sistema
 * mediante GET /api/v1/transactions.
 * Requiere JWT con ROLE_ADMIN para la consulta global;
 * opcionalmente filtra por merchantId.
 */
public class ConsultarTransacciones implements Task {

    private final String merchantIdFiltro;

    public ConsultarTransacciones(String merchantIdFiltro) {
        this.merchantIdFiltro = merchantIdFiltro;
    }

    /** Consulta todas las transacciones (admin). */
    public static ConsultarTransacciones todas() {
        return Tasks.instrumented(ConsultarTransacciones.class, (String) null);
    }

    /** Consulta transacciones filtrando por comercio específico. */
    public static ConsultarTransacciones porMerchant(String merchantId) {
        return Tasks.instrumented(ConsultarTransacciones.class, merchantId);
    }

    /** Consulta transacciones del comercio almacenado en la memoria del actor. */
    public static ConsultarTransacciones delMerchantActual() {
        return Tasks.instrumented(ConsultarTransacciones.class, "use_actor_memory");
    }

    @Override
    @Step("{0} consulta el registro de transacciones")
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

        request.when().get(PaymentEndpoints.LISTAR_TRANSACCIONES);
    }
}
