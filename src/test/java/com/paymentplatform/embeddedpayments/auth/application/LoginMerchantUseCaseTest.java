package com.paymentplatform.embeddedpayments.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentplatform.embeddedpayments.auth.domain.entity.AuthRole;
import com.paymentplatform.embeddedpayments.auth.domain.entity.UserAccount;
import com.paymentplatform.embeddedpayments.auth.infrastructure.repository.UserAccountJpaRepository;
import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantJpaRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.shared.security.JwtTokenService;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginMerchantUseCaseTest {

    @Mock
    private UserAccountJpaRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MerchantJpaRepository merchantRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private LoginMerchantUseCase useCase;

    @Test
    void usuarioNoExiste_lanzaUnauthorized() {
        when(userAccountRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("  A@B.COM ", "secret"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void passwordIncorrecta_lanzaUnauthorized() {
        UserAccount user = user("x@y.com", "hash", "ACTIVE", Set.of(new AuthRole("ROLE_USER", "u")));
        when(userAccountRepository.findByEmail("x@y.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute("x@y.com", "bad"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void usuarioInactivo_lanzaForbidden() {
        UserAccount user = user("i@n.com", "hash", "BLOCKED", Set.of(new AuthRole("ROLE_USER", "u")));
        when(userAccountRepository.findByEmail("i@n.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ok", "hash")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("i@n.com", "ok"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void adminConMerchant_enlazaMerchantIdEnToken() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UserAccount admin = userWithId(userId, "admin@m.com", "hash", "ACTIVE",
                Set.of(new AuthRole("ROLE_ADMIN", "a")));

        when(userAccountRepository.findByEmail("admin@m.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        Merchant m = new Merchant(merchantId, "Shop", "admin@m.com", "ACTIVE");
        when(merchantRepository.findByEmail("admin@m.com")).thenReturn(Optional.of(m));
        when(jwtTokenService.generateUserToken(userId, "ROLE_ADMIN", merchantId)).thenReturn("jwt");
        when(jwtTokenService.extractExpiration("jwt")).thenReturn(Instant.parse("2030-01-01T00:00:00Z"));

        LoginMerchantUseCase.LoginResponse response = useCase.execute("admin@m.com", "secret");

        assertThat(response.token()).isEqualTo("jwt");
        verify(jwtTokenService).generateUserToken(userId, "ROLE_ADMIN", merchantId);
    }

    @Test
    void adminSinMerchantRegistrado_merchantIdNuloEnToken() {
        UUID userId = UUID.randomUUID();
        UserAccount admin = userWithId(userId, "solo@m.com", "hash", "ACTIVE",
                Set.of(new AuthRole("ROLE_ADMIN", "a")));

        when(userAccountRepository.findByEmail("solo@m.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(merchantRepository.findByEmail("solo@m.com")).thenReturn(Optional.empty());
        when(jwtTokenService.generateUserToken(userId, "ROLE_ADMIN", null)).thenReturn("jwt");
        when(jwtTokenService.extractExpiration("jwt")).thenReturn(Instant.now());

        useCase.execute("solo@m.com", "secret");

        verify(jwtTokenService).generateUserToken(userId, "ROLE_ADMIN", null);
    }

    @Test
    void usuarioNormal_loginGeneraTokenSinMerchant() {
        UUID userId = UUID.randomUUID();
        UserAccount user = userWithId(userId, "u@u.com", "hash", "ACTIVE",
                Set.of(new AuthRole("ROLE_USER", "u")));

        when(userAccountRepository.findByEmail("u@u.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtTokenService.generateUserToken(userId, "ROLE_USER", null)).thenReturn("jwt");
        when(jwtTokenService.extractExpiration("jwt")).thenReturn(Instant.now());

        LoginMerchantUseCase.LoginResponse response = useCase.execute("u@u.com", "secret");

        assertThat(response.token()).isEqualTo("jwt");
        verify(jwtTokenService).generateUserToken(userId, "ROLE_USER", null);
    }

    private static UserAccount user(String email, String hash, String status, Set<AuthRole> roles) {
        return new UserAccount(UUID.randomUUID(), email, hash, status, Instant.now(), roles);
    }

    private static UserAccount userWithId(UUID id, String email, String hash, String status, Set<AuthRole> roles) {
        return new UserAccount(id, email, hash, status, Instant.now(), roles);
    }
}
