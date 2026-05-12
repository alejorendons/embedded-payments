package com.paymentplatform.embeddedpayments.auth.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserAccountTest {

    @Test
    void getPrimaryRole_priorizaAdmin() {
        Set<AuthRole> roles = new LinkedHashSet<>();
        roles.add(new AuthRole("ROLE_USER", "u"));
        roles.add(new AuthRole("ROLE_ADMIN", "a"));

        UserAccount account = new UserAccount(UUID.randomUUID(), "e@e.com", "h", "ACTIVE", Instant.now(), roles);

        assertThat(account.getPrimaryRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void getPrimaryRole_priorizaMerchantSobreUser() {
        Set<AuthRole> roles = new LinkedHashSet<>();
        roles.add(new AuthRole("ROLE_USER", "u"));
        roles.add(new AuthRole("ROLE_MERCHANT", "m"));

        UserAccount account = new UserAccount(UUID.randomUUID(), "e@e.com", "h", "ACTIVE", Instant.now(), roles);

        assertThat(account.getPrimaryRole()).isEqualTo("ROLE_MERCHANT");
    }

    @Test
    void getPrimaryRole_sinRolesConocidos_usaPrimeroONulo() {
        Set<AuthRole> roles = new LinkedHashSet<>();
        roles.add(new AuthRole("ROLE_CUSTOM", "c"));

        UserAccount account = new UserAccount(UUID.randomUUID(), "e@e.com", "h", "ACTIVE", Instant.now(), roles);

        assertThat(account.getPrimaryRole()).isEqualTo("ROLE_CUSTOM");
    }

    @Test
    void getPrimaryRole_sinRoles_devuelveUserPorDefecto() {
        UserAccount account = new UserAccount(UUID.randomUUID(), "e@e.com", "h", "ACTIVE", Instant.now(), Set.of());

        assertThat(account.getPrimaryRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void gettersBasicos() {
        UUID id = UUID.randomUUID();
        Instant t = Instant.now();
        UserAccount account = new UserAccount(id, "mail@test.com", "ph", "ACTIVE", t, Set.of());

        assertThat(account.getId()).isEqualTo(id);
        assertThat(account.getEmail()).isEqualTo("mail@test.com");
        assertThat(account.getPasswordHash()).isEqualTo("ph");
        assertThat(account.getStatus()).isEqualTo("ACTIVE");
        assertThat(account.getCreatedAt()).isEqualTo(t);
        assertThat(account.getRoles()).isEmpty();
    }
}
