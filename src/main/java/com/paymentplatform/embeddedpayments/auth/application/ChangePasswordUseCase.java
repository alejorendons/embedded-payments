package com.paymentplatform.embeddedpayments.auth.application;

import com.paymentplatform.embeddedpayments.auth.domain.entity.UserAccount;
import com.paymentplatform.embeddedpayments.auth.infrastructure.repository.UserAccountJpaRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ChangePasswordUseCase {

    private final UserAccountJpaRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordUseCase(UserAccountJpaRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void execute(UUID userId, String currentPassword, String newPassword) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found",
                        List.of()
                ));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new DomainException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS",
                    "Current password is incorrect",
                    List.of()
            );
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new DomainException(
                    HttpStatus.BAD_REQUEST,
                    "WEAK_PASSWORD",
                    "Password must have at least 8 characters",
                    List.of()
            );
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);
    }
}
