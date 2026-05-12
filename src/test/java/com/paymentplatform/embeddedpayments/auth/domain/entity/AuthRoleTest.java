package com.paymentplatform.embeddedpayments.auth.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthRoleTest {

    @Test
    void getters() {
        AuthRole role = new AuthRole("ROLE_ADMIN", "Administrador");

        assertThat(role.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(role.getDescription()).isEqualTo("Administrador");
    }

    @Test
    void equals_y_hashCode_porNombre() {
        AuthRole a = new AuthRole("ROLE_USER", "d1");
        AuthRole b = new AuthRole("ROLE_USER", "d2");

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo("string");
    }
}
