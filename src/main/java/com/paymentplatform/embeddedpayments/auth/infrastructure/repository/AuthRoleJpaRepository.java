package com.paymentplatform.embeddedpayments.auth.infrastructure.repository;

import com.paymentplatform.embeddedpayments.auth.domain.entity.AuthRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRoleJpaRepository extends JpaRepository<AuthRole, Integer> {

    Optional<AuthRole> findByName(String name);
}
