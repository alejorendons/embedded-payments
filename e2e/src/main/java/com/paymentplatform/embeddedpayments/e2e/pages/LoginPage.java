package com.paymentplatform.embeddedpayments.e2e.pages;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class LoginPage {

    public static final Target EMAIL    = Target.the("campo email").located(By.id("email"));
    public static final Target PASSWORD = Target.the("campo password").located(By.id("password"));
    public static final Target BOTON_SIGN_IN = Target.the("botón Sign In")
            .located(By.cssSelector("button[type='submit']"));

    public static final String URL = "http://localhost:5173/login";
}
