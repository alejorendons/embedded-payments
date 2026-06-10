package com.paymentplatform.embeddedpayments.e2e.questions;

import com.paymentplatform.embeddedpayments.e2e.pages.ContactSettingsPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.questions.Text;

public class LaNotificacionMuestra implements Question<String> {

    public static LaNotificacionMuestra elMensajeDeExito() {
        return new LaNotificacionMuestra();
    }

    @Override
    public String answeredBy(Actor actor) {
        return Text.of(ContactSettingsPage.NOTIFICACION_EXITO).answeredBy(actor);
    }
}
