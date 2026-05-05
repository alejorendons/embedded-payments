package com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantStatusHistoryJpaRepository extends JpaRepository<MerchantStatusHistory, UUID> {
    java.util.List<MerchantStatusHistory> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);
}

