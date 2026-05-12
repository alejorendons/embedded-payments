package com.paymentplatform.embeddedpayments.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentplatform.embeddedpayments.shared.security.JwtTokenService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private RefreshTokenUseCase useCase;

    @Test
    void cuandoMerchantSinClaimUsaSubjectComoMerchantId() {
        UUID subjectId = UUID.randomUUID();
        String refresh = "token-bruto";
        Instant exp = Instant.parse("2030-01-01T00:00:00Z");

        when(jwtTokenService.extractSubjectUuid(refresh)).thenReturn(subjectId);
        when(jwtTokenService.extractRole(refresh)).thenReturn("ROLE_MERCHANT");
        when(jwtTokenService.extractMerchantId(refresh)).thenReturn(null);
        when(jwtTokenService.generateUserToken(eq(subjectId), eq("ROLE_MERCHANT"), eq(subjectId)))
                .thenReturn("nuevo-jwt");
        when(jwtTokenService.extractExpiration("nuevo-jwt")).thenReturn(exp);

        RefreshTokenUseCase.RefreshTokenResponse result = useCase.execute(refresh);

        assertThat(result.token()).isEqualTo("nuevo-jwt");
        assertThat(result.expiresAt()).isEqualTo(exp);
        verify(jwtTokenService).generateUserToken(subjectId, "ROLE_MERCHANT", subjectId);
    }

    @Test
    void cuandoMerchantTieneClaimNoSobrescribeMerchantId() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        String refresh = "otro-token";

        when(jwtTokenService.extractSubjectUuid(refresh)).thenReturn(userId);
        when(jwtTokenService.extractRole(refresh)).thenReturn("ROLE_MERCHANT");
        when(jwtTokenService.extractMerchantId(refresh)).thenReturn(merchantId);
        when(jwtTokenService.generateUserToken(eq(userId), eq("ROLE_MERCHANT"), eq(merchantId)))
                .thenReturn("jwt");
        when(jwtTokenService.extractExpiration("jwt")).thenReturn(Instant.now());

        useCase.execute(refresh);

        verify(jwtTokenService).generateUserToken(userId, "ROLE_MERCHANT", merchantId);
    }

    @Test
    void rolDistintoDeMerchantNoAlteraMerchantIdNulo() {
        UUID userId = UUID.randomUUID();
        String refresh = "admin-token";

        when(jwtTokenService.extractSubjectUuid(refresh)).thenReturn(userId);
        when(jwtTokenService.extractRole(refresh)).thenReturn("ROLE_ADMIN");
        when(jwtTokenService.extractMerchantId(refresh)).thenReturn(null);
        when(jwtTokenService.generateUserToken(eq(userId), eq("ROLE_ADMIN"), isNull()))
                .thenReturn("jwt-admin");
        when(jwtTokenService.extractExpiration(any())).thenReturn(Instant.now());

        useCase.execute(refresh);

        verify(jwtTokenService).generateUserToken(eq(userId), eq("ROLE_ADMIN"), isNull());
    }
}
