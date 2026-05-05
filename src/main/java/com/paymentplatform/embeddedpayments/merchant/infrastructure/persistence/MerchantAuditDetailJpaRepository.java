package com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantAuditDetailJpaRepository extends JpaRepository<MerchantAuditDetail, UUID> {
    java.util.List<MerchantAuditDetail> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);
}

