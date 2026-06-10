package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.tasks.AutenticarActor;
import com.paymentplatform.embeddedpayments.e2e.tasks.PrepararComercioActivo;
import com.paymentplatform.embeddedpayments.e2e.tasks.RegistrarCuentaBancaria;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Step definitions para HU 1.3 – Información Financiera del Comercio (pasos Given / When).
 * Los pasos Then comunes están en {@link CommonSteps}.
 */
public class HU13_InformacionFinancieraSteps {

    private static final String API_BASE_URL = "http://localhost:8085";

    private Actor actor = Actor.named("AdminFinanciero");

    @Before
    public void configurarEntorno() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("que el actor está listo para las pruebas de información financiera")
    public void elActorEstaListoParaInfoFinanciera() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("que el comercio está activo")
    public void queElComercioEstaActivo() {
        String email = DataFactory.uniqueEmail("admin.hu13.activo");
        actor.attemptsTo(PrepararComercioActivo.conEmail(email));
    }

    @Dado("que el comercio está inactivo")
    public void queElComercioEstaInactivo() {
        String email = DataFactory.uniqueEmail("admin.hu13.inactivo");
        // Registra un ADMIN con su comercio (queda en INACTIVE) y JWT válido.
        actor.attemptsTo(AutenticarActor.comoAdminConEmail(email));
    }

    @Cuando("registra una cuenta bancaria con datos válidos")
    public void registraCuentaBancariaConDatosValidos() {
        actor.attemptsTo(RegistrarCuentaBancaria.conDatosValidos());
    }

    @Cuando("intenta registrar una cuenta bancaria con datos válidos")
    public void intentaRegistrarCuentaBancariaConDatosValidosInactivo() {
        actor.attemptsTo(RegistrarCuentaBancaria.conDatosValidos());
    }

    @Cuando("intenta registrar una cuenta bancaria con datos inválidos")
    public void intentaRegistrarCuentaBancariaConDatosInvalidos() {
        actor.attemptsTo(RegistrarCuentaBancaria.conDatosInvalidos());
    }
}
