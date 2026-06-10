package com.paymentplatform.embeddedpayments.e2e.stepdefinitions;

import com.paymentplatform.embeddedpayments.e2e.exceptions.E2EException;
import com.paymentplatform.embeddedpayments.e2e.tasks.CrearIntencionPago;
import com.paymentplatform.embeddedpayments.e2e.tasks.PrepararComercioActivo;
import com.paymentplatform.embeddedpayments.e2e.tasks.PrepararMerchantoInactivo;
import com.paymentplatform.embeddedpayments.e2e.utils.DataFactory;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Step definitions para HU 1.9 – Creación de Pagos (pasos Given / When).
 * Los pasos Then comunes están en {@link CommonSteps}.
 */
public class HU19_CreacionPagosSteps {

    private static final String API_BASE_URL = "http://localhost:8085";

    private Actor actor = Actor.named("ComercioPagos");

    @Before
    public void configurarEntorno() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("que el actor está listo para las pruebas de creación de pagos")
    public void elActorEstaListoParaCreacionDePagos() {
        RestAssured.baseURI = API_BASE_URL;
        actor.can(CallAnApi.at(API_BASE_URL));
    }

    @Dado("que el comercio está activo para pagos")
    public void queElComercioEstaActivoParaPagos() {
        String email = DataFactory.uniqueEmail("admin.hu19.activo");
        actor.attemptsTo(PrepararComercioActivo.conEmail(email));
    }

    @Dado("que el comercio está inactivo para pagos")
    public void queElComercioEstaInactivoParaPagos() {
        String nombre = DataFactory.uniqueMerchantName("Comercio-Inactivo-HU19");
        String email = DataFactory.uniqueEmail("hu19.inactivo");
        actor.attemptsTo(PrepararMerchantoInactivo.conNombreYEmail(nombre, email));
    }

    @Cuando("el comercio crea una intención de pago con monto válido")
    public void elComercioCreaUnaIntencionDePagoConMontoValido() {
        actor.attemptsTo(CrearIntencionPago.conMontoValido());
    }

    @Cuando("el comercio intenta crear una intención de pago con monto válido")
    public void elComercioIntentaCrearUnaIntencionDePagoConMontoValido() {
        actor.attemptsTo(CrearIntencionPago.conMontoValido());
    }

    @Cuando("el comercio intenta crear una intención de pago con monto {string}")
    public void elComercioIntentaCrearUnaIntencionDePagoConMonto(String tipoMonto) {
        switch (tipoMonto) {
            case "cero" -> actor.attemptsTo(CrearIntencionPago.conMontoCero());
            case "negativo" -> actor.attemptsTo(CrearIntencionPago.conMontoNegativo());
            default -> throw new E2EException("Tipo de monto no soportado: " + tipoMonto);
        }
    }
}
