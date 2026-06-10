package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.AuthEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task de Screenplay: crea una cuenta de administrador
 * mediante POST /api/v1/auth/register con role=ADMIN.
 *
 * Para el escenario feliz, el actor debe tener JWT de SUPER_ADMIN en memoria.
 * Para el escenario de error por permisos, se envía sin token o con JWT de ADMIN normal.
 */
public class CrearCuentaAdmin implements Task {

    private final String email;
    private final String password;
    private final String role;
    private final String merchantName;
    private final boolean conSuperAdminAuth;

    public CrearCuentaAdmin(String email, String password, String role,
                             String merchantName, boolean conSuperAdminAuth) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.merchantName = merchantName;
        this.conSuperAdminAuth = conSuperAdminAuth;
    }

    /** Crea una cuenta ADMIN autenticándose como Super Admin (token en memoria del actor). */
    public static CrearCuentaAdmin conEmail(String email) {
        return Tasks.instrumented(CrearCuentaAdmin.class,
                email, "Admin1234!", "ADMIN", "Comercio-Admin-E2E", true);
    }

    /** Intenta crear cuenta con datos inválidos. */
    public static CrearCuentaAdmin conDatosInvalidos() {
        return Tasks.instrumented(CrearCuentaAdmin.class,
                "", "", "ADMIN", "", true);
    }

    @Override
    @Step("{0} crea una cuenta de administrador con email '#email'")
    public <T extends Actor> void performAs(T actor) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);
        body.put("merchantName", merchantName);

        var request = SerenityRest.given()
                .contentType("application/json")
                .body(body);

        if (conSuperAdminAuth) {
            String jwt = actor.recall("jwtToken");
            if (jwt != null) {
                request = request.header("Authorization", "Bearer " + jwt);
            }
        }

        request.when().post(AuthEndpoints.REGISTRAR);
    }
}
