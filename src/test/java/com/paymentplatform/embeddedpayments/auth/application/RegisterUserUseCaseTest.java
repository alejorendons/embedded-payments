package com.paymentplatform.embeddedpayments.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentplatform.embeddedpayments.auth.domain.entity.AuthRole;
import com.paymentplatform.embeddedpayments.auth.domain.entity.UserAccount;
import com.paymentplatform.embeddedpayments.auth.infrastructure.repository.AuthRoleJpaRepository;
import com.paymentplatform.embeddedpayments.auth.infrastructure.repository.UserAccountJpaRepository;
import com.paymentplatform.embeddedpayments.merchant.application.RegisterMerchantUseCase;
import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.shared.exception.ConflictException;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserAccountJpaRepository userAccountRepository;

    @Mock
    private AuthRoleJpaRepository authRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterMerchantUseCase registerMerchantUseCase;

    @InjectMocks
    private RegisterUserUseCase useCase;

    @Test
    void rolNull_registraComoUser() {
        AuthRole roleUser = new AuthRole("ROLE_USER", "desc");
        UserAccount saved = org.mockito.Mockito.mock(UserAccount.class);
        when(saved.getId()).thenReturn(UUID.randomUUID());
        when(saved.getEmail()).thenReturn("plain@u.com");
        when(saved.getStatus()).thenReturn("ACTIVE");

        when(userAccountRepository.findByEmail("plain@u.com")).thenReturn(Optional.empty());
        when(authRoleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password12")).thenReturn("encoded");
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(saved);

        RegisterUserUseCase.RegisteredUser out = useCase.execute("plain@u.com", "password12", null, null);

        assertThat(out.role()).isEqualTo("ROLE_USER");
        assertThat(out.merchantId()).isNull();
        verify(registerMerchantUseCase, org.mockito.Mockito.never()).execute(any(), any());
    }

    @Test
    void admin_creaMerchantYUsuario() {
        UUID merchantUuid = UUID.randomUUID();
        Merchant merchant = new Merchant(merchantUuid, "Tienda SA", "adm@m.com", "INACTIVE");
        AuthRole roleAdmin = new AuthRole("ROLE_ADMIN", "a");
        UserAccount saved = org.mockito.Mockito.mock(UserAccount.class);
        UUID userId = UUID.randomUUID();
        when(saved.getId()).thenReturn(userId);
        when(saved.getEmail()).thenReturn("adm@m.com");
        when(saved.getStatus()).thenReturn("ACTIVE");

        when(userAccountRepository.findByEmail("adm@m.com")).thenReturn(Optional.empty());
        when(authRoleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(roleAdmin));
        when(passwordEncoder.encode("password12")).thenReturn("encoded");
        when(registerMerchantUseCase.execute("Tienda SA", "adm@m.com"))
                .thenReturn(new RegisterMerchantUseCase.RegisteredMerchant(merchant, "epk_key"));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(saved);

        RegisterUserUseCase.RegisteredUser out = useCase.execute("adm@m.com", "password12", "ADMIN", "Tienda SA");

        assertThat(out.merchantId()).isEqualTo(merchantUuid);
        assertThat(out.apiKey()).isEqualTo("epk_key");
        assertThat(out.role()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void admin_sinNombreMerchant_lanzaBadRequest() {
        when(userAccountRepository.findByEmail("a@m.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("a@m.com", "password12", "ADMIN", " "))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("merchantName");
    }

    @Test
    void emailDuplicado_lanzaConflict() {
        when(userAccountRepository.findByEmail("dup@m.com"))
                .thenReturn(Optional.of(org.mockito.Mockito.mock(UserAccount.class)));

        assertThatThrownBy(() -> useCase.execute("dup@m.com", "password12", "USER", null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rolInvalido_lanzaBadRequest() {
        assertThatThrownBy(() -> useCase.execute("x@m.com", "password12", "SUPERUSER", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Role must be ADMIN or USER");
    }

    @Test
    void passwordDebil_lanzaBadRequest() {
        assertThatThrownBy(() -> useCase.execute("z@m.com", "short", "USER", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Password");
    }

    @Test
    void rolDesconocidoDesdeBd_sePersisteAuthRoleGenerado() {
        AuthRole nuevo = new AuthRole("ROLE_USER", "Generated by application");
        UserAccount saved = org.mockito.Mockito.mock(UserAccount.class);
        when(saved.getId()).thenReturn(UUID.randomUUID());
        when(saved.getEmail()).thenReturn("gen@m.com");
        when(saved.getStatus()).thenReturn("ACTIVE");

        when(userAccountRepository.findByEmail("gen@m.com")).thenReturn(Optional.empty());
        when(authRoleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
        when(authRoleRepository.save(any(AuthRole.class))).thenReturn(nuevo);
        when(passwordEncoder.encode("password12")).thenReturn("enc");
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(saved);

        useCase.execute("gen@m.com", "password12", "USER", null);

        verify(authRoleRepository).save(any(AuthRole.class));
    }
}
