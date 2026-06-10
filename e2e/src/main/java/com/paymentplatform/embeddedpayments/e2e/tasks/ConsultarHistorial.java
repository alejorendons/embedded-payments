package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.PaymentEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

/**
 * Task de Screenplay: consulta el historial de operaciones (intenciones de pago)
 * del comercio mediante GET /api/v1/payments/intents.
 * Autenticación: X-API-Key almacenada en la memoria del actor.
 * Soporta filtro opcional por rango de fechas.
 */
public class ConsultarHistorial implements Task {

    private final String fechaDesde;
    private final String fechaHasta;

    public ConsultarHistorial(String fechaDesde, String fechaHasta) {
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    /** Consulta el historial completo de operaciones sin filtros. */
    public static ConsultarHistorial completo() {
        return Tasks.instrumented(ConsultarHistorial.class, null, null);
    }

    /** Consulta el historial con un rango de fechas específico. */
    public static ConsultarHistorial conRangoFechas(String desde, String hasta) {
        return Tasks.instrumented(ConsultarHistorial.class, desde, hasta);
    }

    @Override
    @Step("{0} consulta el historial de operaciones")
    public <T extends Actor> void performAs(T actor) {
        String apiKey = actor.recall("apiKey");

        var request = SerenityRest.given()
                .header("X-API-Key", apiKey)
                .contentType("application/json");

        if (fechaDesde != null) {
            request = request.queryParam("from", fechaDesde);
        }
        if (fechaHasta != null) {
            request = request.queryParam("to", fechaHasta);
        }

        request.when().get(PaymentEndpoints.HISTORIAL_INTENCIONES);
    }
}
