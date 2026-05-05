package com.paymentplatform.embeddedpayments.auth.infrastructure.repository;

import com.paymentplatform.embeddedpayments.auth.domain.entity.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountJpaRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByEmail(String email);
}
