package com.paymentplatform.embeddedpayments.auth.domain.repository;

import com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey;
import java.util.Optional;
import java.util.UUID;

public interface MerchantApiKeyRepository {

    MerchantApiKey save(MerchantApiKey apiKey);

    Optional<MerchantApiKey> findById(UUID id);
}
