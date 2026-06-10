package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.pages.ContactSettingsPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actions.Clear;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Open;
import org.openqa.selenium.JavascriptExecutor;

public class ActualizarContactoUI implements Task {

    private final String nombre;
    private final String email;

    public ActualizarContactoUI(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    public static ActualizarContactoUI con(String nombre, String email) {
        return Tasks.instrumented(ActualizarContactoUI.class, nombre, email);
    }

    @Override
    @Step("{0} abre la pantalla de contacto, completa nombre '#nombre' y email '#email', y guarda")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Open.url(ContactSettingsPage.URL),
                Clear.field(ContactSettingsPage.CAMPO_NOMBRE),
                Enter.theValue(nombre).into(ContactSettingsPage.CAMPO_NOMBRE),
                Clear.field(ContactSettingsPage.CAMPO_EMAIL),
                Enter.theValue(email).into(ContactSettingsPage.CAMPO_EMAIL)
        );

        // Deshabilita validación nativa de HTML5 para que Vue pueda manejar el submit
        JavascriptExecutor js = (JavascriptExecutor) BrowseTheWeb.as(actor).getDriver();
        js.executeScript("document.querySelectorAll('form').forEach(f => f.setAttribute('novalidate', ''));");

        actor.attemptsTo(Click.on(ContactSettingsPage.BOTON_GUARDAR));

        // Espera a que Vue procese el submit y actualice el DOM
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
    }
}
