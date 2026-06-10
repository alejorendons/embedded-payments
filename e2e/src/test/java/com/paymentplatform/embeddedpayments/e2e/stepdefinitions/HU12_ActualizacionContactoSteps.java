package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.tasks.ActualizarContacto;
import com.paymentplatform.embeddedpayments.e2e.tasks.AutenticarActor;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Step definitions para HU 1.2 – Actualización de Información de Contacto (pasos Given / When).
 *
 * Los pasos Then comunes están en {@link CommonSteps}.
 * Sigue el patrón Screenplay: Actor → Tasks → Questions.
 * Las llamadas HTTP van contra la API real en http://localhost:8085.
 *
 * Flujo de Background:
 * 1. Registra un usuario ADMIN (que también crea un Comercio INACTIVE).
 * 2. Hace login y obtiene un JWT con ROLE_ADMIN.
 * 3. Guarda merchantId y jwtToken en la memoria del actor para los escenarios.
 */
public class HU12_ActualizacionContactoSteps {

    private static final String API_BASE_URL = "http://localhost:8085";

    private Actor actor = Actor.named("AdminComercio");

    // ── Setup ─────────────────────────────────────────────────────────────────

    @Before
    public void configurarEntorno() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    // ── Background ────────────────────────────────────────────────────────────

    @Dado("que el actor está listo para las pruebas de actualización de contacto")
    public void elActorEstaListoParaActualizacion() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("se ha registrado un administrador con su comercio para las pruebas")
    public void seHaRegistradoUnAdministradorConSuComercio() {
        String uniqueEmail = DataFactory.uniqueEmail("admin.hu12");
        actor.attemptsTo(AutenticarActor.comoAdminConEmail(uniqueEmail));
    }

    // ── Given ─────────────────────────────────────────────────────────────────

    @Dado("el actor está autenticado como administrador")
    public void elActorEstaAutenticadoComoAdministrador() {
        // El JWT ya fue almacenado en la memoria del actor durante el Background.
        // Este paso sirve como documentación explícita del contexto del escenario.
    }

    // ── When ─────────────────────────────────────────────────────────────────

    @Cuando("actualiza el contacto con nombre {string} y email {string}")
    public void actualizaElContacto(String nombre, String email) {
        actor.attemptsTo(ActualizarContacto.con(nombre, email));
    }

    @Cuando("intenta actualizar el contacto de un comercio que no existe en el sistema")
    public void intentaActualizarContactoDeComercioInexistente() {
        String idInexistente = DataFactory.randomUUID();
        actor.attemptsTo(
                ActualizarContacto.conMerchantId("Nombre Test", "test@test.com", idInexistente)
        );
    }

    @Cuando("actualiza el contacto sin enviar token con nombre {string} y email {string}")
    public void actualizaContactoSinToken(String nombre, String email) {
        String merchantId = actor.recall("merchantId");
        String idParaUsar = merchantId != null ? merchantId : DataFactory.randomUUID();
        actor.attemptsTo(ActualizarContacto.sinAutenticacion(nombre, email, idParaUsar));
    }
}
