package com.paymentplatform.embeddedpayments.e2e.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * Runner para las pruebas E2E de interfaz de usuario (navegador).
 *
 * Requisitos antes de ejecutar:
 *   1. Servidor API corriendo en http://localhost:8085
 *   2. Frontend Vue corriendo en http://localhost:5173  (npm run dev)
 *
 * Para correr sólo estas pruebas: {@code mvn test -Dcucumber.filter.tags="@UI"}
 */
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.paymentplatform.embeddedpayments.e2e.stepdefinitions",
        tags = "@UI",
        snippets = CucumberOptions.SnippetType.CAMELCASE,
        plugin = {
                "pretty",
                "html:target/cucumber-reports/ui/index.html",
                "json:target/cucumber-reports/ui/cucumber.json"
        }
)
public class EmbeddedPaymentsUIRunner {
}
