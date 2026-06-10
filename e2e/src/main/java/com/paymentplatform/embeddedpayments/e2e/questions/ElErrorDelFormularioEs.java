package com.paymentplatform.embeddedpayments.e2e.questions;

import com.paymentplatform.embeddedpayments.e2e.pages.ContactSettingsPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.questions.Text;

public class ElErrorDelFormularioEs implements Question<String> {

    public enum Campo { NOMBRE, EMAIL }

    private final Campo campo;

    public ElErrorDelFormularioEs(Campo campo) {
        this.campo = campo;
    }

    public static ElErrorDelFormularioEs delCampoNombre() {
        return new ElErrorDelFormularioEs(Campo.NOMBRE);
    }

    public static ElErrorDelFormularioEs delCampoEmail() {
        return new ElErrorDelFormularioEs(Campo.EMAIL);
    }

    @Override
    public String answeredBy(Actor actor) {
        return switch (campo) {
            case NOMBRE -> Text.of(ContactSettingsPage.ERROR_NOMBRE).answeredBy(actor);
            case EMAIL  -> Text.of(ContactSettingsPage.ERROR_EMAIL).answeredBy(actor);
        };
    }
}
