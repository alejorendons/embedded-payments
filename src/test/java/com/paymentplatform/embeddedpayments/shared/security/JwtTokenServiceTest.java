package com.paymentplatform.embeddedpayments.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Usa el mismo secreto que {@code src/test/resources/application.properties}.
 */
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(
                "test-secret-key-test-secret-key-test-secret-key",
                "embedded-payments-test",
                60L);
    }

    @Test
    void generateUserToken_incluyeClaimMerchantIdCuandoNoEsNulo() {
        UUID user = UUID.randomUUID();
        UUID merchant = UUID.randomUUID();

        String token = jwtTokenService.generateUserToken(user, "ROLE_ADMIN", merchant);

        assertThat(jwtTokenService.extractSubjectUuid(token)).isEqualTo(user);
        assertThat(jwtTokenService.extractRole(token)).isEqualTo("ROLE_ADMIN");
        assertThat(jwtTokenService.extractMerchantId(token)).isEqualTo(merchant);
    }

    @Test
    void extractRole_defaultMerchantCuandoClaimAusente() {
        UUID mid = UUID.randomUUID();
        String token = jwtTokenService.generateMerchantToken(mid);

        assertThat(jwtTokenService.extractRole(token)).isEqualTo("ROLE_MERCHANT");
    }

    @Test
    void extractMerchantId_desdeClaim() {
        UUID user = UUID.randomUUID();
        UUID merchant = UUID.randomUUID();
        String token = jwtTokenService.generateUserToken(user, "ROLE_ADMIN", merchant);

        assertThat(jwtTokenService.extractMerchantId(token)).isEqualTo(merchant);
    }

    @Test
    void extractMerchantId_rolMerchantSinClaim_usaSubject() {
        UUID merchant = UUID.randomUUID();
        String token = jwtTokenService.generateMerchantToken(merchant);

        assertThat(jwtTokenService.extractMerchantId(token)).isEqualTo(merchant);
    }

    @Test
    void extractExpiration_devuelveInstanteFuturo() {
        UUID id = UUID.randomUUID();
        String token = jwtTokenService.generateUserToken(id, "ROLE_USER", null);
        java.time.Instant now = java.time.Instant.now();

        assertThat(jwtTokenService.extractExpiration(token)).isAfter(now.minusSeconds(5));
    }

    @Test
    void generateToken_aliasDelegaAMerchantToken() {
        UUID mid = UUID.randomUUID();

        String t1 = jwtTokenService.generateToken(mid);
        String t2 = jwtTokenService.generateMerchantToken(mid);

        assertThat(jwtTokenService.extractSubjectUuid(t1)).isEqualTo(mid);
        assertThat(jwtTokenService.extractSubjectUuid(t2)).isEqualTo(mid);
    }
}
