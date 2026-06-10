package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.AdminEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task de Screenplay: actualiza los datos personales del administrador autenticado
 * mediante PUT /api/v1/auth/me.
 */
public class ActualizarInfoAdmin implements Task {

    private final String nuevoEmail;
    private final String nuevoNombre;

    public ActualizarInfoAdmin(String nuevoEmail, String nuevoNombre) {
        this.nuevoEmail = nuevoEmail;
        this.nuevoNombre = nuevoNombre;
    }

    /** Actualiza datos personales con valores válidos. */
    public static ActualizarInfoAdmin conEmailYNombre(String email, String nombre) {
        return Tasks.instrumented(ActualizarInfoAdmin.class, email, nombre);
    }

    /** Intenta actualizar con email ya registrado en otro usuario. */
    public static ActualizarInfoAdmin conEmailDuplicado(String emailDuplicado) {
        return Tasks.instrumented(ActualizarInfoAdmin.class, emailDuplicado, "Nombre Válido");
    }

    /** Intenta actualizar con datos inválidos. */
    public static ActualizarInfoAdmin conDatosInvalidos() {
        return Tasks.instrumented(ActualizarInfoAdmin.class, "no-es-email", "");
    }

    @Override
    @Step("{0} actualiza su información de perfil con email '#nuevoEmail'")
    public <T extends Actor> void performAs(T actor) {
        String jwt = actor.recall("jwtToken");

        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", nuevoEmail);
        body.put("name", nuevoNombre);

        SerenityRest.given()
                .header("Authorization", "Bearer " + jwt)
                .contentType("application/json")
                .body(body)
                .when()
                .put(AdminEndpoints.ACTUALIZAR_PERFIL);
    }
}
