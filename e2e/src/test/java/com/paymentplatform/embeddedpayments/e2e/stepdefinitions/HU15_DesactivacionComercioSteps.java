package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.tasks.CrearIntencionPago;
import com.paymentplatform.embeddedpayments.e2e.tasks.DesactivarComercio;
import com.paymentplatform.embeddedpayments.e2e.tasks.PrepararComercioActivo;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Step definitions para HU 1.5 – Desactivación de Comercio (pasos Given / When).
 * Los pasos Then comunes están en {@link CommonSteps}.
 */
public class HU15_DesactivacionComercioSteps {

    private static final String API_BASE_URL = "http://localhost:8085";

    private Actor actor = Actor.named("AdminDesactivacion");

    @Before
    public void configurarEntorno() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("que el actor está listo para las pruebas de desactivación de comercio")
    public void elActorEstaListoParaDesactivacion() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("se ha registrado un administrador con un comercio activo")
    public void seHaRegistradoUnAdministradorConComercioActivo() {
        String email = DataFactory.uniqueEmail("admin.hu15");
        actor.attemptsTo(PrepararComercioActivo.conEmail(email));
    }

    @Cuando("el administrador desactiva el comercio")
    public void elAdministradorDesactivaElComercio() {
        actor.attemptsTo(DesactivarComercio.elComercioActual());
    }

    @Dado("el administrador ha desactivado el comercio")
    public void elAdministradorHaDesactivadoElComercio() {
        actor.attemptsTo(DesactivarComercio.elComercioActual());
    }

    @Cuando("el comercio desactivado intenta crear una intención de pago")
    public void elComercioDesactivadoIntentaCrearIntencionDePago() {
        actor.attemptsTo(CrearIntencionPago.conMontoValido());
    }
}
