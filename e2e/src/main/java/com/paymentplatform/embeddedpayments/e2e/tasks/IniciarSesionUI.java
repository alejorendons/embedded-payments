package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.pages.LoginPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Open;

public class IniciarSesionUI implements Task {

    private final String email;
    private final String password;

    public IniciarSesionUI(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static IniciarSesionUI con(String email, String password) {
        return Tasks.instrumented(IniciarSesionUI.class, email, password);
    }

    @Override
    @Step("{0} inicia sesión en la UI con email '#email'")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Open.url(LoginPage.URL),
                Enter.theValue(email).into(LoginPage.EMAIL),
                Enter.theValue(password).into(LoginPage.PASSWORD),
                Click.on(LoginPage.BOTON_SIGN_IN)
        );
    }
}
