package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.AuthEndpoints;
import com.paymentplatform.embeddedpayments.e2e.exceptions.E2EException;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task de Screenplay (setup): registra un usuario con rol SUPER_ADMIN
 * y hace login para obtener el JWT.
 *
 * <p>Almacena en la memoria del actor:
 * <ul>
 *   <li>{@code jwtToken}   – Bearer token con ROLE_SUPER_ADMIN.</li>
 *   <li>{@code adminEmail} – Email del Super Admin registrado.</li>
 * </ul>
 */
public class AutenticarComoSuperAdmin implements Task {

    private static final String PASSWORD = "SuperAdmin1234!";
    private static final String MERCHANT_NAME = "Plataforma-E2E-SuperAdmin";

    private final String email;

    public AutenticarComoSuperAdmin(String email) {
        this.email = email;
    }

    public static AutenticarComoSuperAdmin conEmail(String email) {
        return Tasks.instrumented(AutenticarComoSuperAdmin.class, email);
    }

    @Override
    @Step("{0} se registra como SUPER_ADMIN con email '#email' y obtiene el JWT")
    public <T extends Actor> void performAs(T actor) {
        Map<String, String> registerBody = new LinkedHashMap<>();
        registerBody.put("email", email);
        registerBody.put("password", PASSWORD);
        registerBody.put("role", "SUPER_ADMIN");
        registerBody.put("merchantName", MERCHANT_NAME);

        Response registerResponse = SerenityRest.given()
                .contentType("application/json")
                .body(registerBody)
                .post(AuthEndpoints.REGISTRAR);

        if (registerResponse.statusCode() != 201) {
            throw new E2EException(
                    "No se pudo registrar el Super Admin. Status: " + registerResponse.statusCode()
                            + " | Body: " + registerResponse.asString());
        }

        Map<String, String> loginBody = new LinkedHashMap<>();
        loginBody.put("email", email);
        loginBody.put("password", PASSWORD);

        Response loginResponse = SerenityRest.given()
                .contentType("application/json")
                .body(loginBody)
                .post(AuthEndpoints.LOGIN);

        if (loginResponse.statusCode() != 200) {
            throw new E2EException(
                    "Login de Super Admin fallido. Status: " + loginResponse.statusCode()
                            + " | Body: " + loginResponse.asString());
        }

        String token = loginResponse.jsonPath().getString("token");
        actor.remember("jwtToken", token);
        actor.remember("adminEmail", email);
    }
}
