package com.paymentplatform.embeddedpayments.auth.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MerchantApiKeyRepositoryImplTest {

    @Mock
    private MerchantApiKeyJpaRepository jpaRepository;

    @InjectMocks
    private MerchantApiKeyRepositoryImpl repository;

    @Test
    void saveDelegaAJpa() {
        MerchantApiKey key = new MerchantApiKey(UUID.randomUUID(), UUID.randomUUID(), "h", "ACTIVE", Instant.now());
        when(jpaRepository.save(key)).thenReturn(key);

        assertThat(repository.save(key)).isSameAs(key);
        verify(jpaRepository).save(key);
    }

    @Test
    void findByIdDelegaAJpa() {
        UUID id = UUID.randomUUID();
        MerchantApiKey key = new MerchantApiKey(id, UUID.randomUUID(), "h", "ACTIVE", Instant.now());
        when(jpaRepository.findById(id)).thenReturn(Optional.of(key));

        assertThat(repository.findById(id)).contains(key);
    }
}
