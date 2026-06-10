package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.tasks.ActivarComercio;
import com.paymentplatform.embeddedpayments.e2e.tasks.AutenticarActor;
import com.paymentplatform.embeddedpayments.e2e.tasks.ConsultarComercio;
import com.paymentplatform.embeddedpayments.e2e.tasks.RegistrarCuentaBancaria;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Step definitions para HU 1.6 – Consulta de Comercio por ID (pasos Given / When).
 * Los pasos Then comunes están en {@link CommonSteps}.
 */
public class HU16_ConsultaComercioSteps {

    private static final String API_BASE_URL = "http://localhost:8085";

    private Actor actor = Actor.named("AdminConsulta");

    @Before
    public void configurarEntorno() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("que el actor está listo para las pruebas de consulta de comercio")
    public void elActorEstaListoParaConsulta() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("se ha registrado un administrador con su comercio para consulta")
    public void seHaRegistradoUnAdministradorConSuComercioParaConsulta() {
        String email = DataFactory.uniqueEmail("admin.hu16");
        actor.attemptsTo(AutenticarActor.comoAdminConEmail(email));
    }

    @Dado("el comercio tiene una cuenta bancaria registrada")
    public void elComercioTieneUnaCuentaBancariaRegistrada() {
        actor.attemptsTo(ActivarComercio.elComercioActual());
        actor.attemptsTo(RegistrarCuentaBancaria.conDatosValidos());
    }

    @Cuando("el administrador consulta el comercio por su ID")
    public void elAdministradorConsultaElComercioPorSuId() {
        actor.attemptsTo(ConsultarComercio.elComercioActual());
    }

    @Cuando("el administrador consulta un comercio con ID inexistente")
    public void elAdministradorConsultaComercioInexistente() {
        actor.attemptsTo(ConsultarComercio.conId(DataFactory.randomUUID()));
    }

    @Cuando("un usuario sin autenticación consulta el comercio por su ID")
    public void unUsuarioSinAutenticacionConsultaElComercio() {
        actor.attemptsTo(ConsultarComercio.sinAutenticacion());
    }
}
