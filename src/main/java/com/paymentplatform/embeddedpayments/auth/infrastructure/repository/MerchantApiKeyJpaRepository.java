package com.paymentplatform.embeddedpayments.auth.infrastructure.repository;

import com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantApiKeyJpaRepository extends JpaRepository<MerchantApiKey, UUID> {
}
