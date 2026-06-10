package com.paymentplatform.embeddedpayments.e2e.questions;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

/**
 * Question de Screenplay: verifica que el cuerpo de error de la respuesta siga
 * la estructura estándar de la plataforma de pagos embebidos.
 *
 * <p>Campos obligatorios siempre: {@code errorCode}, {@code message}, {@code traceId}.<br>
 * Campo adicional cuando {@code checkDetails = true}: {@code details} (array).
 *
 * Uso:
 * <pre>
 *   actor.should(seeThat("error body válido",
 *       ElCuerpoDeErrorEsValido.conErrorCode("VALIDATION_ERROR").yConDetails(), equalTo(true)));
 *
 *   actor.should(seeThat("error body válido",
 *       ElCuerpoDeErrorEsValido.conErrorCode("MERCHANT_NOT_FOUND").sinDetails(), equalTo(true)));
 * </pre>
 */
public class ElCuerpoDeErrorEsValido implements Question<Boolean> {

    private final String expectedErrorCode;
    private final boolean checkDetails;

    public ElCuerpoDeErrorEsValido(String expectedErrorCode, boolean checkDetails) {
        this.expectedErrorCode = expectedErrorCode;
        this.checkDetails = checkDetails;
    }

    public static ElCuerpoDeErrorEsValido conErrorCode(String errorCode) {
        return new ElCuerpoDeErrorEsValido(errorCode, false);
    }

    public ElCuerpoDeErrorEsValido yConDetails() {
        return new ElCuerpoDeErrorEsValido(this.expectedErrorCode, true);
    }

    public ElCuerpoDeErrorEsValido sinDetails() {
        return new ElCuerpoDeErrorEsValido(this.expectedErrorCode, false);
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        Response response = SerenityRest.lastResponse();
        String actualErrorCode = response.jsonPath().getString("errorCode");
        String message = response.jsonPath().getString("message");
        String traceId = response.jsonPath().getString("traceId");

        boolean errorCodeOk = expectedErrorCode.equals(actualErrorCode);
        boolean messageOk = message != null && !message.isEmpty();
        boolean traceIdOk = traceId != null && !traceId.isEmpty();

        boolean detailsOk = true;
        if (checkDetails) {
            Object details = response.jsonPath().get("details");
            detailsOk = details != null;
        }

        return errorCodeOk && messageOk && traceIdOk && detailsOk;
    }
}
