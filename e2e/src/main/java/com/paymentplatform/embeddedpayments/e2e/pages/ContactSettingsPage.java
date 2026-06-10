package com.paymentplatform.embeddedpayments.e2e.pages;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class ContactSettingsPage {

    public static final Target CAMPO_NOMBRE = Target.the("campo Contact Name")
            .located(By.id("contactName"));
    public static final Target CAMPO_EMAIL  = Target.the("campo Contact Email")
            .located(By.id("contactEmail"));
    public static final Target BOTON_GUARDAR = Target.the("botón Save Changes")
            .located(By.cssSelector("button[type='submit']"));

    public static final Target ERROR_NOMBRE = Target.the("error Contact Name")
            .located(By.xpath("//input[@id='contactName']/following-sibling::p"));
    public static final Target ERROR_EMAIL  = Target.the("error Contact Email")
            .located(By.xpath("//input[@id='contactEmail']/following-sibling::p"));

    public static final Target NOTIFICACION_EXITO = Target.the("notificación de éxito")
            .located(By.cssSelector(".bg-green-600"));
    public static final Target NOTIFICACION_ERROR = Target.the("notificación de error")
            .located(By.cssSelector(".bg-red-600"));

    public static final String URL = "http://localhost:5173/settings/contact";
}
