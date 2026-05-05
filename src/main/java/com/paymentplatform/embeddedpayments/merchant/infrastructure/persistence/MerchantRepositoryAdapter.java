package com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantRepositoryAdapter implements MerchantRepository {

    private final MerchantJpaRepository merchantJpaRepository;

    public MerchantRepositoryAdapter(MerchantJpaRepository merchantJpaRepository) {
        this.merchantJpaRepository = merchantJpaRepository;
    }

    @Override
    public Merchant save(Merchant merchant) {
        return merchantJpaRepository.save(merchant);
    }

    @Override
    public Optional<Merchant> findById(UUID merchantId) {
        return merchantJpaRepository.findById(merchantId);
    }

    @Override
    public Optional<Merchant> findByEmail(String email) {
        return merchantJpaRepository.findByEmail(email);
    }
}

