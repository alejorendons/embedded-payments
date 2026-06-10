package com.paymentplatform.embeddedpayments.e2e.tasks;

import com.paymentplatform.embeddedpayments.e2e.endpoints.MerchantEndpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task de Screenplay: registra la cuenta bancaria de un comercio
 * mediante PUT /api/v1/merchants/{id}/bank-account.
 *
 * Uso:
 * <pre>
 *   actor.attemptsTo(RegistrarCuentaBancaria.conDatosValidos());
 *   actor.attemptsTo(RegistrarCuentaBancaria.conDatosInvalidos());
 *   actor.attemptsTo(RegistrarCuentaBancaria.paraMerchantoInactivo());
 * </pre>
 */
public class RegistrarCuentaBancaria implements Task {

    private final String iban;
    private final String routingNumber;
    private final String accountHolderName;
    private final String merchantIdOverride;

    public RegistrarCuentaBancaria(String iban, String routingNumber,
                                    String accountHolderName, String merchantIdOverride) {
        this.iban = iban;
        this.routingNumber = routingNumber;
        this.accountHolderName = accountHolderName;
        this.merchantIdOverride = merchantIdOverride;
    }

    public static RegistrarCuentaBancaria conDatosValidos() {
        return Tasks.instrumented(RegistrarCuentaBancaria.class,
                "GB29NWBK60161331926819", "021000089", "Comercio E2E Test", null);
    }

    public static RegistrarCuentaBancaria conDatosInvalidos() {
        return Tasks.instrumented(RegistrarCuentaBancaria.class,
                "IBAN-INVALIDO", "RUTA-INVALIDA", "", null);
    }

    public static RegistrarCuentaBancaria paraMerchantoConId(String merchantId) {
        return Tasks.instrumented(RegistrarCuentaBancaria.class,
                "GB29NWBK60161331926819", "021000089", "Comercio E2E Test", merchantId);
    }

    @Override
    @Step("{0} registra cuenta bancaria con IBAN '#iban'")
    public <T extends Actor> void performAs(T actor) {
        String jwt = actor.recall("jwtToken");
        String id = merchantIdOverride != null ? merchantIdOverride : actor.recall("merchantId");

        Map<String, String> body = new LinkedHashMap<>();
        body.put("iban", iban);
        body.put("routing_number", routingNumber);
        body.put("account_holder_name", accountHolderName);

        SerenityRest.given()
                .header("Authorization", "Bearer " + jwt)
                .contentType("application/json")
                .body(body)
                .when()
                .put(MerchantEndpoints.REGISTRAR_CUENTA_BANCARIA.replace("{id}", id));
    }
}
