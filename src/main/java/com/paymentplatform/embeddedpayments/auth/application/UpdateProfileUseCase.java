package com.paymentplatform.embeddedpayments.auth.application;

import com.paymentplatform.embeddedpayments.auth.domain.entity.UserAccount;
import com.paymentplatform.embeddedpayments.auth.infrastructure.repository.UserAccountJpaRepository;
import com.paymentplatform.embeddedpayments.shared.exception.ConflictException;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UpdateProfileUseCase {

    private final UserAccountJpaRepository userAccountRepository;

    public UpdateProfileUseCase(UserAccountJpaRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount execute(UUID userId, String email, String name) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found",
                        List.of()
                ));

        String normalizedEmail = email.trim().toLowerCase();
        if (!normalizedEmail.equals(user.getEmail())) {
            userAccountRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
                throw new ConflictException(
                        "EMAIL_ALREADY_REGISTERED",
                        "Email is already registered",
                        List.of("email: " + normalizedEmail)
                );
            });
        }

        user.updateProfile(normalizedEmail, name);
        return userAccountRepository.save(user);
    }
}
