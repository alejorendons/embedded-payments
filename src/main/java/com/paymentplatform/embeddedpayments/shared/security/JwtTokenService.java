package com.paymentplatform.embeddedpayments.shared.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_MERCHANT_ID = "merchantId";

    private final SecretKey secretKey;
    private final String issuer;
    private final long expirationMinutes;

    public JwtTokenService(@Value("${app.security.jwt.secret}") String secret,
                           @Value("${app.security.jwt.issuer}") String issuer,
                           @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateMerchantToken(UUID merchantId) {
        return generateTokenInternal(merchantId, "ROLE_MERCHANT", merchantId);
    }

    public String generateUserToken(UUID userId, String role, UUID merchantId) {
        return generateTokenInternal(userId, role, merchantId);
    }

    private String generateTokenInternal(UUID subjectId, String role, UUID merchantId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        var builder = Jwts.builder()
                .subject(subjectId.toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(CLAIM_ROLE, role);

        if (merchantId != null) {
            builder.claim(CLAIM_MERCHANT_ID, merchantId.toString());
        }

        return builder.signWith(secretKey).compact();
    }

    public String generateToken(UUID merchantId) {
        return generateMerchantToken(merchantId);
    }

    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public UUID extractMerchantId(String token) {
        Claims claims = parseClaims(token);
        Object merchantId = claims.get(CLAIM_MERCHANT_ID);
        if (merchantId != null) {
            return UUID.fromString(merchantId.toString());
        }

        String role = extractRole(token);
        if ("ROLE_MERCHANT".equals(role)) {
            return UUID.fromString(claims.getSubject());
        }
        return null;
    }

    public UUID extractSubjectUuid(String token) {
        return UUID.fromString(extractSubject(token));
    }

    public String extractRole(String token) {
        Claims claims = parseClaims(token);
        Object role = claims.get(CLAIM_ROLE);
        if (role == null) {
            return "ROLE_MERCHANT";
        }
        return role.toString();
    }

    public Instant extractExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.toInstant();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}


