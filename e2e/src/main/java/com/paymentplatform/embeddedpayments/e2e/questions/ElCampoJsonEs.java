package com.paymentplatform.embeddedpayments.e2e.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

/**
 * Question de Screenplay: extrae el valor de un campo del JSON de la última respuesta.
 *
 * Uso en step definitions:
 * <pre>
 *   actor.should(seeThat("El estado del comercio", ElCampoJsonEs.elCampo("status"), equalTo("INACTIVE")));
 *   actor.should(seeThat("La api_key", ElCampoJsonEs.elCampo("api_key"), startsWith("epk_")));
 * </pre>
 */
public class ElCampoJsonEs implements Question<String> {

    private final String jsonPath;

    public ElCampoJsonEs(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public static ElCampoJsonEs elCampo(String jsonPath) {
        return new ElCampoJsonEs(jsonPath);
    }

    @Override
    public String answeredBy(Actor actor) {
        Object value = SerenityRest.lastResponse().jsonPath().get(jsonPath);
        return value != null ? value.toString() : null;
    }
}
