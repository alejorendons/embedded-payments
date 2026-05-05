package com.paymentplatform.embeddedpayments.auth.infrastructure.repository;

import com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey;
import com.paymentplatform.embeddedpayments.auth.domain.repository.MerchantApiKeyRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantApiKeyRepositoryImpl implements MerchantApiKeyRepository {

    private final MerchantApiKeyJpaRepository jpaRepository;

    public MerchantApiKeyRepositoryImpl(MerchantApiKeyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MerchantApiKey save(MerchantApiKey apiKey) {
        return jpaRepository.save(apiKey);
    }

    @Override
    public Optional<MerchantApiKey> findById(UUID id) {
        return jpaRepository.findById(id);
    }
}
