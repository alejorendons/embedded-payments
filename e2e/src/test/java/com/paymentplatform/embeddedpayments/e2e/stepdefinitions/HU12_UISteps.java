package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.questions.ElErrorDelFormularioEs;
import com.paymentplatform.embeddedpayments.e2e.questions.LaNotificacionMuestra;
import com.paymentplatform.embeddedpayments.e2e.tasks.ActualizarContactoUI;
import com.paymentplatform.embeddedpayments.e2e.tasks.AutenticarActor;
import com.paymentplatform.embeddedpayments.e2e.tasks.IniciarSesionUI;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.After;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Step definitions UI para HU 1.2 – actualización de contacto desde el navegador.
 * Patrón: Screenplay con BrowseTheWeb + WebDriver.
 * Requisito: servidor API en http://localhost:8085 y frontend en http://localhost:5173.
 */
public class HU12_UISteps {

    private static final String PASSWORD = "Admin1234!";

    private WebDriver driver;
    private Actor actor;
    private String adminEmail;

    // ── Setup ────────────────────────────────────────────────────────────────

    @Dado("existe una cuenta de administrador lista para las pruebas UI")
    public void existeUnaCuentaDeAdministradorListaParaLasPruebasUI() {
        adminEmail = DataFactory.uniqueEmail("admin.ui");

        // Registra el admin vía REST (para que exista en BD) usando un actor temporal
        Actor apiActor = Actor.named("ApiSetup");
        apiActor.attemptsTo(AutenticarActor.comoAdminConEmail(adminEmail));

        // Inicializa WebDriver con WebDriverManager
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized", "--remote-allow-origins=*");
        driver = new ChromeDriver(options);

        actor = Actor.named("UsuarioUI");
        actor.can(BrowseTheWeb.with(driver));
    }

    @Dado("el actor ha iniciado sesión en la plataforma web")
    public void elActorHaIniciadoSesionEnLaPlataformaWeb() {
        actor.attemptsTo(IniciarSesionUI.con(adminEmail, PASSWORD));
        WebDriver webDriver = BrowseTheWeb.as(actor).getDriver();
        // Espera a que el browser llegue al dashboard (router redirect tras login)
        new WebDriverWait(webDriver, Duration.ofSeconds(15))
                .until(d -> d.getCurrentUrl().contains("/dashboard"));
        // Espera a que el merchant se cargue en el store (fetchById es async)
        try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
    }

    // ── When ─────────────────────────────────────────────────────────────────

    @Cuando("completa el formulario de contacto con nombre {string} y email {string}")
    public void completaElFormularioDeContacto(String nombre, String email) {
        actor.attemptsTo(ActualizarContactoUI.con(nombre, email));
    }

    @Cuando("envía el formulario de contacto con nombre {string} y email {string}")
    public void enviaElFormularioDeContacto(String nombre, String email) {
        actor.attemptsTo(ActualizarContactoUI.con(nombre, email));
    }

    // ── Then ─────────────────────────────────────────────────────────────────

    @Entonces("ve la notificación de éxito {string}")
    public void veLaNotificacionDeExito(String mensajeEsperado) {
        actor.should(seeThat(LaNotificacionMuestra.elMensajeDeExito(),
                containsString(mensajeEsperado)));
    }

    @Entonces("el formulario muestra el error de nombre {string}")
    public void elFormularioMuestraElErrorDeNombre(String mensajeEsperado) {
        actor.should(seeThat(ElErrorDelFormularioEs.delCampoNombre(),
                containsString(mensajeEsperado)));
    }

    @Entonces("el formulario muestra el error de email {string}")
    public void elFormularioMuestraElErrorDeEmail(String mensajeEsperado) {
        actor.should(seeThat(ElErrorDelFormularioEs.delCampoEmail(),
                containsString(mensajeEsperado)));
    }

    // ── Teardown ─────────────────────────────────────────────────────────────

    @After(order = 100)
    public void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }
}
