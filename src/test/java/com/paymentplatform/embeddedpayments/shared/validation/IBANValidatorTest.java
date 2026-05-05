package com.paymentplatform.embeddedpayments.shared.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class IBANValidatorTest {

    private IBANValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new IBANValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void debeAceptarIBANValido() {
        // IBAN español válido con checksum correcto
        assertThat(validator.isValid("ES9121000418450200051332", context)).isTrue();
    }

    @Test
    void debeRechazarIBANNulo() {
        assertThat(validator.isValid(null, context)).isFalse();
    }

    @Test
    void debeRechazarIBANBlanco() {
        assertThat(validator.isValid("   ", context)).isFalse();
    }

    @Test
    void debeRechazarIBANConFormatoInvalido() {
        // Empieza con minúsculas — no cumple el patrón ^[A-Z]{2}...
        assertThat(validator.isValid("es9121000418450200051332", context)).isFalse();
    }

    @Test
    void debeRechazarIBANConChecksumIncorrecto() {
        // Mismo IBAN español pero con checksum incorrecto (cambiamos el 91 por 00)
        assertThat(validator.isValid("ES0021000418450200051332", context)).isFalse();
    }

    @Test
    void debeAceptarIBANAleman() {
        // IBAN alemán válido
        assertThat(validator.isValid("DE89370400440532013000", context)).isTrue();
    }
}
