package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.tasks.ActivarComercio;
import com.paymentplatform.embeddedpayments.e2e.tasks.AutenticarActor;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Step definitions para HU 1.4 – Activación de Comercio (pasos Given / When).
 * Los pasos Then comunes están en {@link CommonSteps}.
 */
public class HU14_ActivacionComercioSteps {

    private static final String API_BASE_URL = "http://localhost:8085";

    private Actor actor = Actor.named("AdminActivacion");

    @Before
    public void configurarEntorno() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("que el actor está listo para las pruebas de activación de comercio")
    public void elActorEstaListoParaActivacion() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("se ha registrado un administrador con un comercio inactivo")
    public void seHaRegistradoUnAdministradorConComercioInactivo() {
        String email = DataFactory.uniqueEmail("admin.hu14");
        actor.attemptsTo(AutenticarActor.comoAdminConEmail(email));
    }

    @Cuando("el administrador activa el comercio")
    public void elAdministradorActivaElComercio() {
        actor.attemptsTo(ActivarComercio.elComercioActual());
    }

    @Cuando("el administrador intenta activar un comercio con ID inexistente")
    public void elAdministradorIntentaActivarComercioInexistente() {
        actor.attemptsTo(ActivarComercio.conId(DataFactory.randomUUID()));
    }
}
