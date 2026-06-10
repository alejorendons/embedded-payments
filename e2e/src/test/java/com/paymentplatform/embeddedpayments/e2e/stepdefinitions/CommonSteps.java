package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.questions.ElCampoJsonEs;
import com.paymentplatform.embeddedpayments.e2e.questions.ElCodigoDeRespuestaEs;
import com.paymentplatform.embeddedpayments.e2e.questions.ElCuerpoDeErrorEsValido;
import io.cucumber.java.es.Entonces;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.*;


public class CommonSteps {

    private Actor actor = Actor.named("Verificador");

    // ── Código HTTP ───────────────────────────────────────────────────────────

    @Entonces("el código de respuesta HTTP es {int}")
    public void elCodigoDeRespuestaHTTPEs(int codigoEsperado) {
        actor.should(
                seeThat("El código HTTP de la respuesta",
                        ElCodigoDeRespuestaEs.elCodigo(),
                        equalTo(codigoEsperado))
        );
    }

    // ── Campos JSON ───────────────────────────────────────────────────────────

    @Entonces("el campo {string} de la respuesta es {string}")
    public void elCampoDeRespuestaEs(String campo, String valorEsperado) {
        actor.should(
                seeThat("El campo '" + campo + "' de la respuesta",
                        ElCampoJsonEs.elCampo(campo),
                        equalTo(valorEsperado))
        );
    }

    @Entonces("el campo {string} de la respuesta comienza con {string}")
    public void elCampoDeRespuestaComienzaCon(String campo, String prefijo) {
        actor.should(
                seeThat("El campo '" + campo + "' comienza con '" + prefijo + "'",
                        ElCampoJsonEs.elCampo(campo),
                        startsWith(prefijo))
        );
    }

    @Entonces("el campo {string} de la respuesta no está vacío")
    public void elCampoNoEstaVacio(String campo) {
        actor.should(
                seeThat("El campo '" + campo + "' no está vacío",
                        ElCampoJsonEs.elCampo(campo),
                        not(emptyOrNullString()))
        );
    }

    @Entonces("el campo {string} de la respuesta contiene {string}")
    public void elCampoDeRespuestaContiene(String campo, String fragmento) {
        actor.should(
                seeThat("El campo '" + campo + "' contiene '" + fragmento + "'",
                        ElCampoJsonEs.elCampo(campo),
                        containsString(fragmento))
        );
    }

    // ── Listas ────────────────────────────────────────────────────────────────

    @Entonces("la respuesta contiene una lista no vacía en el campo {string}")
    public void laRespuestaContieneUnaListaNoVaciaEnElCampo(String campo) {
        java.util.List<?> lista = SerenityRest.lastResponse().jsonPath().getList(campo);
        net.serenitybdd.screenplay.Question<Integer> size = a -> lista != null ? lista.size() : 0;
        actor.should(seeThat("El tamaño de la lista '" + campo + "'", size, greaterThan(0)));
    }

    @Entonces("la respuesta contiene una lista vacía en el campo {string}")
    public void laRespuestaContieneUnaListaVaciaEnElCampo(String campo) {
        java.util.List<?> lista = SerenityRest.lastResponse().jsonPath().getList(campo);
        net.serenitybdd.screenplay.Question<Integer> size = a -> lista != null ? lista.size() : 0;
        actor.should(seeThat("La lista '" + campo + "' está vacía", size, equalTo(0)));
    }

    // ── Cuerpo de error ───────────────────────────────────────────────────────

    @Entonces("el cuerpo de error contiene el errorCode {string}")
    public void elCuerpoDeErrorContieneErrorCode(String errorCode) {
        actor.should(
                seeThat("El errorCode del cuerpo de error",
                        ElCampoJsonEs.elCampo("errorCode"),
                        equalTo(errorCode))
        );
    }

    @Entonces("el cuerpo de error contiene los campos obligatorios message, details y traceId")
    public void elCuerpoDeErrorContieneMessageDetailsYTraceId() {
        actor.should(
                seeThat("El cuerpo de error tiene estructura estándar con details",
                        ElCuerpoDeErrorEsValido.conErrorCode(leerErrorCode()).yConDetails(),
                        equalTo(true))
        );
    }

    @Entonces("el cuerpo de error contiene los campos obligatorios message y traceId")
    public void elCuerpoDeErrorContieneMessageYTraceId() {
        actor.should(
                seeThat("El cuerpo de error tiene estructura estándar",
                        ElCuerpoDeErrorEsValido.conErrorCode(leerErrorCode()).sinDetails(),
                        equalTo(true))
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String leerErrorCode() {
        String code = SerenityRest.lastResponse().jsonPath().getString("errorCode");
        return code != null ? code : "";
    }
}
