package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.tasks.ActualizarInfoAdmin;
import com.paymentplatform.embeddedpayments.e2e.tasks.AutenticarActor;
import com.paymentplatform.embeddedpayments.e2e.tasks.CambiarContrasena;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Step definitions para HU 1.8 – Gestión de Información del Administrador (pasos Given / When).
 * Los pasos Then comunes están en {@link CommonSteps}.
 */
public class HU18_GestionInfoAdminSteps {

    private static final String API_BASE_URL = "http://localhost:8085";

    private Actor actor = Actor.named("AdminPerfil");
    private String otroAdminEmail;

    @Before
    public void configurarEntorno() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("que el actor está listo para las pruebas de gestión de información del administrador")
    public void elActorEstaListoParaGestionDePerfil() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("se ha registrado y autenticado un administrador para gestionar su perfil")
    public void seHaRegistradoYAutenticadoUnAdministradorParaGestionarSuPerfil() {
        String email = DataFactory.uniqueEmail("admin.hu18");
        actor.attemptsTo(AutenticarActor.comoAdminConEmail(email));
    }

    @Cuando("el administrador actualiza sus datos personales con información válida")
    public void elAdministradorActualizaSusDatosPersonalesConInformacionValida() {
        String nuevoEmail = DataFactory.uniqueEmail("actualizado.hu18");
        actor.attemptsTo(ActualizarInfoAdmin.conEmailYNombre(nuevoEmail, "Nombre Actualizado"));
    }

    @Cuando("el administrador cambia su contraseña exitosamente")
    public void elAdministradorCambiaSuContrasenaExitosamente() {
        actor.attemptsTo(CambiarContrasena.exitosamente());
    }

    @Cuando("el administrador intenta cambiar su contraseña con la contraseña actual incorrecta")
    public void elAdministradorIntentaCambiarSuContrasenaConLaContrasenaActualIncorrecta() {
        actor.attemptsTo(CambiarContrasena.conContrasenaActualIncorrecta());
    }

    @Dado("existe otro administrador registrado con un email distinto")
    public void existeOtroAdministradorRegistradoConUnEmailDistinto() {
        otroAdminEmail = DataFactory.uniqueEmail("otro.admin.hu18");
        Actor otroActor = Actor.named("OtroAdmin");
        otroActor.can(CallAnApi.at(API_BASE_URL));
        otroActor.attemptsTo(AutenticarActor.comoAdminConEmail(otroAdminEmail));
    }

    @Cuando("el administrador intenta actualizar su perfil con el email de otro administrador")
    public void elAdministradorIntentaActualizarSuPerfilConElEmailDeOtroAdministrador() {
        actor.attemptsTo(ActualizarInfoAdmin.conEmailDuplicado(otroAdminEmail));
    }

    @Cuando("el administrador intenta actualizar su perfil con datos inválidos")
    public void elAdministradorIntentaActualizarSuPerfilConDatosInvalidos() {
        actor.attemptsTo(ActualizarInfoAdmin.conDatosInvalidos());
    }
}
