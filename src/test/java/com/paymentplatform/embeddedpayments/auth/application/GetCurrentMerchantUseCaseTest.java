package com.paymentplatform.embeddedpayments.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.paymentplatform.embeddedpayments.auth.domain.entity.AuthRole;
import com.paymentplatform.embeddedpayments.auth.domain.entity.UserAccount;
import com.paymentplatform.embeddedpayments.auth.infrastructure.repository.UserAccountJpaRepository;
import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantJpaRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCurrentMerchantUseCaseTest {

    @Mock
    private MerchantJpaRepository merchantRepository;

    @Mock
    private UserAccountJpaRepository userAccountRepository;

    @InjectMocks
    private GetCurrentMerchantUseCase useCase;

    @Test
    void rolMerchant_resuelvePorMerchantIdClaim() {
        UUID merchantId = UUID.randomUUID();
        Merchant m = new Merchant(merchantId, "Tienda", "t@x.com", "ACTIVE");

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(m));

        GetCurrentMerchantUseCase.CurrentMerchantResponse r =
                useCase.execute(UUID.randomUUID(), "ROLE_MERCHANT", merchantId);

        assertThat(r.email()).isEqualTo("t@x.com");
        assertThat(r.merchantId()).isEqualTo(merchantId);
    }

    @Test
    void rolMerchant_sinClaim_usaCurrentUserIdComoMerchantId() {
        UUID uid = UUID.randomUUID();
        Merchant m = new Merchant(uid, "T", "e@e.com", "ACTIVE");

        when(merchantRepository.findById(uid)).thenReturn(Optional.of(m));

        GetCurrentMerchantUseCase.CurrentMerchantResponse r =
                useCase.execute(uid, "ROLE_MERCHANT", null);

        assertThat(r.id()).isEqualTo(uid);
        assertThat(r.merchantId()).isEqualTo(uid);
    }

    @Test
    void rolMerchant_lanzaCuandoComercioNoExiste() {
        UUID mid = UUID.randomUUID();
        when(merchantRepository.findById(mid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), "ROLE_MERCHANT", mid))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Merchant not found");
    }

    @Test
    void rolAdmin_resuelveMerchantPorEmailCuandoNoHayClaim() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UserAccount user = new UserAccount(userId, "admin@shop.com", "h", "ACTIVE", Instant.now(),
                Set.of(new AuthRole("ROLE_ADMIN", "a")));

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        Merchant m = new Merchant(merchantId, "Shop", "admin@shop.com", "ACTIVE");
        when(merchantRepository.findByEmail("admin@shop.com")).thenReturn(Optional.of(m));

        GetCurrentMerchantUseCase.CurrentMerchantResponse r =
                useCase.execute(userId, "ROLE_ADMIN", null);

        assertThat(r.merchantId()).isEqualTo(merchantId);
        assertThat(r.email()).isEqualTo("admin@shop.com");
    }

    @Test
    void usuarioRegular_sinMerchantAsociado() {
        UUID userId = UUID.randomUUID();
        UserAccount user = new UserAccount(userId, "user@u.com", "h", "ACTIVE", Instant.now(),
                Set.of(new AuthRole("ROLE_USER", "u")));

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));

        GetCurrentMerchantUseCase.CurrentMerchantResponse r =
                useCase.execute(userId, "ROLE_USER", null);

        assertThat(r.merchantId()).isNull();
        assertThat(r.role()).isEqualTo("ROLE_USER");
    }

    @Test
    void usuarioNoEncontrado_lanza() {
        UUID userId = UUID.randomUUID();
        when(userAccountRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(userId, "ROLE_USER", null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("User not found");
    }
}
