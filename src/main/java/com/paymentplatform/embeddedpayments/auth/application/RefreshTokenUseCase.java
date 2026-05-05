package com.paymentplatform.embeddedpayments.auth.application;

import com.paymentplatform.embeddedpayments.shared.security.JwtTokenService;
import java.util.UUID;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RefreshTokenUseCase {

    private final JwtTokenService jwtTokenService;

    public RefreshTokenUseCase(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public RefreshTokenResponse execute(String refreshToken) {
        UUID subjectId = jwtTokenService.extractSubjectUuid(refreshToken);
        String role = jwtTokenService.extractRole(refreshToken);
        UUID merchantId = jwtTokenService.extractMerchantId(refreshToken);

        if ("ROLE_MERCHANT".equals(role) && merchantId == null) {
            merchantId = subjectId;
        }

        String newToken = jwtTokenService.generateUserToken(subjectId, role, merchantId);
        Instant expiresAt = jwtTokenService.extractExpiration(newToken);

        return new RefreshTokenResponse(newToken, expiresAt);
    }

    public record RefreshTokenResponse(String token, Instant expiresAt) {
    }
}
