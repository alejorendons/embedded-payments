package com.paymentplatform.embeddedpayments.auth.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MerchantApiKeyTest {

    @Test
    void isActive_trueCuandoStatusACTIVE() {
        MerchantApiKey key = new MerchantApiKey(UUID.randomUUID(), UUID.randomUUID(), "h", "ACTIVE", Instant.now());
        assertThat(key.isActive()).isTrue();
    }

    @Test
    void isActive_falseCuandoNoACTIVE() {
        MerchantApiKey key = new MerchantApiKey(UUID.randomUUID(), UUID.randomUUID(), "h", "REVOKED", Instant.now());
        assertThat(key.isActive()).isFalse();
    }

    @Test
    void getters() {
        UUID id = UUID.randomUUID();
        UUID mid = UUID.randomUUID();
        Instant t = Instant.now();
        MerchantApiKey key = new MerchantApiKey(id, mid, "hash", "ACTIVE", t);

        assertThat(key.getId()).isEqualTo(id);
        assertThat(key.getMerchantId()).isEqualTo(mid);
        assertThat(key.getKeyHash()).isEqualTo("hash");
        assertThat(key.getStatus()).isEqualTo("ACTIVE");
        assertThat(key.getCreatedAt()).isEqualTo(t);
    }
}
