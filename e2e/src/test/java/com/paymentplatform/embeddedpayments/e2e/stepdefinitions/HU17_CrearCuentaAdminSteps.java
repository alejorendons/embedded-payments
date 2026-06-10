package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.tasks.AutenticarComoSuperAdmin;
import com.paymentplatform.embeddedpayments.e2e.tasks.CrearCuentaAdmin;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Step definitions para HU 1.7 – Creación de Cuentas de Administrador (pasos Given / When).
 * Los pasos Then comunes están en {@link CommonSteps}.
 */
public class HU17_CrearCuentaAdminSteps {

    private static final String API_BASE_URL = "http://localhost:8085";

    private Actor actor = Actor.named("SuperAdmin");
    private String nuevoAdminEmail;

    @Before
    public void configurarEntorno() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("que el actor está listo para las pruebas de creación de cuentas de administrador")
    public void elActorEstaListoParaCreacionDeCuentas() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("se ha autenticado un Super Administrador para las pruebas")
    public void seHaAutenticadoUnSuperAdministrador() {
        String email = DataFactory.uniqueEmail("superadmin.hu17");
        actor.attemptsTo(AutenticarComoSuperAdmin.conEmail(email));
    }

    @Cuando("el Super Admin crea una cuenta de administrador")
    public void elSuperAdminCreaUnaCuentaDeAdministrador() {
        nuevoAdminEmail = DataFactory.uniqueEmail("nuevo.admin.hu17");
        actor.attemptsTo(CrearCuentaAdmin.conEmail(nuevoAdminEmail));
    }

    @Dado("ya existe una cuenta de administrador creada por el Super Admin")
    public void yaExisteUnaCuentaDeAdministradorCreadaPorElSuperAdmin() {
        nuevoAdminEmail = DataFactory.uniqueEmail("duplicado.admin.hu17");
        actor.attemptsTo(CrearCuentaAdmin.conEmail(nuevoAdminEmail));
    }

    @Cuando("el Super Admin intenta crear otra cuenta de administrador con el mismo email")
    public void elSuperAdminIntentaCrearOtraCuentaConElMismoEmail() {
        actor.attemptsTo(CrearCuentaAdmin.conEmail(nuevoAdminEmail));
    }

    @Cuando("el Super Admin intenta crear una cuenta de administrador con datos inválidos")
    public void elSuperAdminIntentaCrearUnaCuentaConDatosInvalidos() {
        actor.attemptsTo(CrearCuentaAdmin.conDatosInvalidos());
    }
}
