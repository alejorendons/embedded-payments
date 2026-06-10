package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.AuthEndpoints;
import com.paymentplatform.embeddedpayments.e2e.endpoints.MerchantEndpoints;
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
 * Task de setup: registra un usuario ADMIN, obtiene el JWT, almacena el apiKey
 * y activa el comercio asociado para que quede listo para pruebas de pago.
 *
 * <p>Almacena en la memoria del actor:
 * <ul>
 *   <li>{@code jwtToken}   – Bearer token para llamadas autenticadas.</li>
 *   <li>{@code merchantId} – UUID del comercio creado.</li>
 *   <li>{@code apiKey}     – API key (epk_...) para llamadas con X-API-Key.</li>
 *   <li>{@code adminEmail} – Email del administrador.</li>
 * </ul>
 */
public class PrepararComercioActivo implements Task {

    private static final String PASSWORD = "Admin1234!";
    private static final String MERCHANT_NAME = "Comercio-E2E-Pagos";

    private final String email;

    public PrepararComercioActivo(String email) {
        this.email = email;
    }

    public static PrepararComercioActivo conEmail(String email) {
        return Tasks.instrumented(PrepararComercioActivo.class, email);
    }

    @Override
    @Step("{0} prepara un comercio ACTIVO con email '#email' y obtiene el apiKey")
    public <T extends Actor> void performAs(T actor) {
        Map<String, String> registerBody = new LinkedHashMap<>();
        registerBody.put("email", email);
        registerBody.put("password", PASSWORD);
        registerBody.put("role", "ADMIN");
        registerBody.put("merchantName", MERCHANT_NAME);

        Response registerResponse = SerenityRest.given()
                .contentType("application/json")
                .body(registerBody)
                .post(AuthEndpoints.REGISTRAR);

        if (registerResponse.statusCode() != 201) {
            throw new E2EException(
                    "Registro de admin fallido. Status: " + registerResponse.statusCode()
                            + " | Body: " + registerResponse.asString());
        }

        String merchantId = registerResponse.jsonPath().getString("merchantId");
        String apiKey = registerResponse.jsonPath().getString("apiKey");

        Map<String, String> loginBody = new LinkedHashMap<>();
        loginBody.put("email", email);
        loginBody.put("password", PASSWORD);

        Response loginResponse = SerenityRest.given()
                .contentType("application/json")
                .body(loginBody)
                .post(AuthEndpoints.LOGIN);

        if (loginResponse.statusCode() != 200) {
            throw new E2EException(
                    "Login fallido. Status: " + loginResponse.statusCode()
                            + " | Body: " + loginResponse.asString());
        }

        String jwt = loginResponse.jsonPath().getString("token");

        actor.remember("jwtToken", jwt);
        actor.remember("merchantId", merchantId);
        actor.remember("apiKey", apiKey);
        actor.remember("adminEmail", email);

        Map<String, String> activateBody = new LinkedHashMap<>();
        activateBody.put("reason", "Activación automática para pruebas E2E");

        SerenityRest.given()
                .header("Authorization", "Bearer " + jwt)
                .contentType("application/json")
                .body(activateBody)
                .patch(MerchantEndpoints.ACTIVAR.replace("{id}", merchantId));
    }
}
