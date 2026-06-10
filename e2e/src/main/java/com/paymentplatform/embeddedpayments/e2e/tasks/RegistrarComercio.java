package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.MerchantEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

import java.util.LinkedHashMap;
import java.util.Map;


public class RegistrarComercio implements Task {

    private final String nombre;
    private final String email;
    private final Boolean esAdministrador;

    public RegistrarComercio(String nombre, String email, Boolean esAdministrador) {
        this.nombre = nombre;
        this.email = email;
        this.esAdministrador = esAdministrador;
    }

    /** Registro público (comercio potencial). */
    public static RegistrarComercio con(String nombre, String email) {
        return Tasks.instrumented(RegistrarComercio.class, nombre, email, false);
    }

    /** Registro realizado por un administrador autenticado. */
    public static RegistrarComercio comoAdministrador(String nombre, String email) {
        return Tasks.instrumented(RegistrarComercio.class, nombre, email, true);
    }

    @Override
    @Step("{0} registra el comercio '#nombre' con email '#email'")
    public <T extends Actor> void performAs(T actor) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("name", nombre);
        body.put("email", email);

        var request = SerenityRest.given()
                .contentType("application/json")
                .body(body);

        if (esAdministrador) {
            String jwt = actor.recall("jwtToken");
            if (jwt != null) {
                request = request.header("Authorization", "Bearer " + jwt);
            }
        }

        request.when().post(MerchantEndpoints.REGISTRAR);
    }
}
