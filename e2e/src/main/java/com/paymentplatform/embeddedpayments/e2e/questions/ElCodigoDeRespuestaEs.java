package com.paymentplatform.embeddedpayments.e2e.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

/**
 * Question de Screenplay: devuelve el código de estado HTTP de la última respuesta.
 *
 * Uso en step definitions:
 * <pre>
 *   actor.should(seeThat("El código HTTP", ElCodigoDeRespuestaEs.elCodigo(), equalTo(201)));
 * </pre>
 */
public class ElCodigoDeRespuestaEs implements Question<Integer> {

    public static ElCodigoDeRespuestaEs elCodigo() {
        return new ElCodigoDeRespuestaEs();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        return SerenityRest.lastResponse().getStatusCode();
    }
}
