package com.paymentplatform.embeddedpayments.auth.application;

import com.paymentplatform.embeddedpayments.auth.domain.entity.UserAccount;
import com.paymentplatform.embeddedpayments.auth.infrastructure.repository.UserAccountJpaRepository;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantJpaRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.shared.security.JwtTokenService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LoginMerchantUseCase {

    private final UserAccountJpaRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final MerchantJpaRepository merchantRepository;
    private final JwtTokenService jwtTokenService;

    public LoginMerchantUseCase(UserAccountJpaRepository userAccountRepository,
                                PasswordEncoder passwordEncoder,
                                MerchantJpaRepository merchantRepository,
                                JwtTokenService jwtTokenService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.merchantRepository = merchantRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResponse execute(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        UserAccount user = userAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS",
                        "Invalid email or password",
                        List.of()
                ));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new DomainException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS",
                    "Invalid email or password",
                    List.of()
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new DomainException(
                    HttpStatus.FORBIDDEN,
                    "USER_INACTIVE",
                    "User account is not active",
                    List.of("userId: " + user.getId())
            );
        }

        String role = user.getPrimaryRole();
        UUID merchantId = null;
        if ("ROLE_ADMIN".equals(role)) {
            merchantId = merchantRepository.findByEmail(normalizedEmail)
                    .map(merchant -> merchant.getId())
                    .orElse(null);
        }

        String token = jwtTokenService.generateUserToken(user.getId(), role, merchantId);
        Instant expiresAt = jwtTokenService.extractExpiration(token);

        return new LoginResponse(token, expiresAt);
    }

    public record LoginResponse(String token, Instant expiresAt) {
    }
}
