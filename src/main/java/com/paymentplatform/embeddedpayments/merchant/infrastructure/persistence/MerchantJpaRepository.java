package com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantJpaRepository extends JpaRepository<Merchant, UUID> {
    
    java.util.Optional<Merchant> findByEmail(String email);
    
    java.util.Optional<Merchant> findByContactEmail(String contactEmail);
}

