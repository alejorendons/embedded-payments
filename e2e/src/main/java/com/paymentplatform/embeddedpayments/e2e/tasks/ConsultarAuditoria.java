package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.AdminEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

/**
 * Task de Screenplay: consulta el registro de auditoría de cambios de estado
 * mediante GET /api/v1/audit.
 * Requiere JWT con ROLE_ADMIN.
 * Soporta filtro opcional por fecha.
 */
public class ConsultarAuditoria implements Task {

    private final String fechaFiltro;

    public ConsultarAuditoria(String fechaFiltro) {
        this.fechaFiltro = fechaFiltro;
    }

    /** Consulta todos los registros de auditoría sin filtro. */
    public static ConsultarAuditoria todos() {
        return Tasks.instrumented(ConsultarAuditoria.class, (String) null);
    }

    /** Consulta registros de auditoría filtrando por fecha (formato ISO: YYYY-MM-DD). */
    public static ConsultarAuditoria porFecha(String fecha) {
        return Tasks.instrumented(ConsultarAuditoria.class, fecha);
    }

    @Override
    @Step("{0} consulta el historial de auditoría")
    public <T extends Actor> void performAs(T actor) {
        String jwt = actor.recall("jwtToken");

        var request = SerenityRest.given()
                .header("Authorization", "Bearer " + jwt)
                .contentType("application/json");

        if (fechaFiltro != null) {
            request = request.queryParam("date", fechaFiltro);
        }

        request.when().get(AdminEndpoints.AUDITORIA);
    }
}
