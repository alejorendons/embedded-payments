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
 * Task de Screenplay (setup): registra un usuario ADMIN —lo que también crea
 * su Comercio— y luego hace login para obtener el JWT.
 *
 * <p>Almacena en la memoria del actor:
 * <ul>
 *   <li>{@code jwtToken}   – Bearer token para llamadas autenticadas.</li>
 *   <li>{@code merchantId} – UUID del comercio creado junto con el usuario ADMIN.</li>
 *   <li>{@code adminEmail} – Email del administrador registrado.</li>
 * </ul>
 *
 * Uso:
 * <pre>
 *   actor.attemptsTo(AutenticarActor.comoAdminConEmail("admin@e2etest.com"));
 * </pre>
 */
public class AutenticarActor implements Task {

    private static final String PASSWORD = "Admin1234!";
    private static final String MERCHANT_NAME = "Comercio-E2E-Admin";

    private final String email;

    public AutenticarActor(String email) {
        this.email = email;
    }

    public static AutenticarActor comoAdminConEmail(String email) {
        return Tasks.instrumented(AutenticarActor.class, email);
    }

    @Override
    @Step("{0} se registra como ADMIN con email '#email' y obtiene el JWT")
    public <T extends Actor> void performAs(T actor) {
        String merchantId = registrarAdmin();
        String token = login();

        actor.remember("jwtToken", token);
        actor.remember("merchantId", merchantId);
        actor.remember("adminEmail", email);
    }

    // ── helpers privados ──────────────────────────────────────────────────────

    private String registrarAdmin() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", PASSWORD);
        body.put("role", "ADMIN");
        body.put("merchantName", MERCHANT_NAME);

        Response response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .post(AuthEndpoints.REGISTRAR);

        if (response.statusCode() != 201) {
            throw new E2EException(
                    "No se pudo registrar el administrador. Status: " + response.statusCode()
                            + " | Body: " + response.asString());
        }

        // El endpoint POST /api/v1/auth/register retorna merchantId para rol ADMIN.
        String merchantId = response.jsonPath().getString("merchantId");
        if (merchantId == null || merchantId.isBlank()) {
            throw new E2EException(
                    "El registro de ADMIN no retornó merchantId. Body: " + response.asString());
        }
        return merchantId;
    }

    private String login() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", PASSWORD);

        Response response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .post(AuthEndpoints.LOGIN);

        if (response.statusCode() != 200) {
            throw new E2EException(
                    "Login fallido. Status: " + response.statusCode()
                            + " | Body: " + response.asString());
        }

        String token = response.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            throw new E2EException("El login no retornó un token JWT. Body: " + response.asString());
        }
        return token;
    }
}
