package com.paymentplatform.embeddedpayments.shared.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RoutingNumberValidatorTest {

    private RoutingNumberValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new RoutingNumberValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void debeAceptarRoutingNumberValido() {
        // 021000089 → checksum: (0+2*7+1*3)+(0+0*7+0*3)+(0+8*7+9*3) = 17+0+83 = 100 → 100%10=0 ✓
        assertThat(validator.isValid("021000089", context)).isTrue();
    }

    @Test
    void debeAceptarOtroRoutingNumberValido() {
        // 111000025 → checksum válido alternativo
        assertThat(validator.isValid("111000025", context)).isTrue();
    }

    @Test
    void debeRechazarRoutingNumberNulo() {
        assertThat(validator.isValid(null, context)).isFalse();
    }

    @Test
    void debeRechazarRoutingNumberBlanco() {
        assertThat(validator.isValid("   ", context)).isFalse();
    }

    @Test
    void debeRechazarRoutingNumberConMenosDe9Digitos() {
        assertThat(validator.isValid("12345678", context)).isFalse();
    }

    @Test
    void debeRechazarRoutingNumberConLetras() {
        assertThat(validator.isValid("12345678A", context)).isFalse();
    }

    @Test
    void debeRechazarRoutingNumberConChecksumIncorrecto() {
        // 9 dígitos pero checksum inválido
        assertThat(validator.isValid("000000001", context)).isFalse();
    }
}
