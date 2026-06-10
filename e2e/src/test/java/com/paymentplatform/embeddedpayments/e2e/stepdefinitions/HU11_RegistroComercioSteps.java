package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.tasks.RegistrarComercio;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import java.util.UUID;

public class HU11_RegistroComercioSteps {

    private static final String API_BASE_URL = "http://localhost:8085";

    private Actor actor = Actor.named("ComercioPotencial");
    private String scenarioSuffix;

    // ── Setup ─────────────────────────────────────────────────────────────────

    @Before
    public void configurarEntorno() {
        scenarioSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    // transforma "foo@bar.com" → "foo+<suffix>@bar.com"; deja intactos los emails inválidos
    private String emailUnico(String base) {
        int at = base.indexOf('@');
        if (at < 0) return base;
        return base.substring(0, at) + "+" + scenarioSuffix + base.substring(at);
    }

    // ── Background ────────────────────────────────────────────────────────────

    @Dado("que el actor está listo para las pruebas de registro de comercio")
    public void elActorEstaListoParaRegistro() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    // ── Given ─────────────────────────────────────────────────────────────────

    @Dado("que ya existe un comercio con email {string}")
    public void existeUnComercioConEmail(String email) {
        actor.attemptsTo(RegistrarComercio.con(DataFactory.uniqueMerchantName("Previo"), emailUnico(email)));
    }

    // ── When ─────────────────────────────────────────────────────────────────

    @Cuando("registra un comercio con nombre {string} y email {string}")
    public void registraUnComercio(String nombre, String email) {
        actor.attemptsTo(RegistrarComercio.con(nombre, emailUnico(email)));
    }

    @Cuando("el administrador registra un comercio con nombre {string} y email {string}")
    public void administradorRegistraUnComercio(String nombre, String email) {
        actor.attemptsTo(RegistrarComercio.comoAdministrador(nombre, emailUnico(email)));
    }

    @Cuando("intenta registrar un comercio con nombre {string} y email {string}")
    public void intentaRegistrarUnComercioInvalido(String nombre, String email) {
        actor.attemptsTo(RegistrarComercio.con(nombre, emailUnico(email)));
    }
}
