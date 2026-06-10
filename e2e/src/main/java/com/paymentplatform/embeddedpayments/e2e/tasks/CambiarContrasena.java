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
 * Task de Screenplay: cambia la contraseña del administrador autenticado
 * mediante POST /api/v1/auth/change-password.
 */
public class CambiarContrasena implements Task {

    private static final String PASSWORD_ORIGINAL = "Admin1234!";

    private final String currentPassword;
    private final String newPassword;

    public CambiarContrasena(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    /** Cambia la contraseña con la contraseña actual correcta. */
    public static CambiarContrasena exitosamente() {
        return Tasks.instrumented(CambiarContrasena.class, PASSWORD_ORIGINAL, "NuevaAdmin5678!");
    }

    /** Intenta cambiar contraseña con la contraseña actual incorrecta. */
    public static CambiarContrasena conContrasenaActualIncorrecta() {
        return Tasks.instrumented(CambiarContrasena.class, "ContraseñaIncorrecta!", "NuevaAdmin5678!");
    }

    @Override
    @Step("{0} intenta cambiar su contraseña")
    public <T extends Actor> void performAs(T actor) {
        String jwt = actor.recall("jwtToken");

        Map<String, String> body = new LinkedHashMap<>();
        body.put("current_password", currentPassword);
        body.put("new_password", newPassword);

        SerenityRest.given()
                .header("Authorization", "Bearer " + jwt)
                .contentType("application/json")
                .body(body)
                .when()
                .post(AdminEndpoints.CAMBIAR_CONTRASENA);
    }
}
