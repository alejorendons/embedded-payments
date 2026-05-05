package com.paymentplatform.embeddedpayments.auth.application;

import com.paymentplatform.embeddedpayments.auth.domain.entity.UserAccount;
import com.paymentplatform.embeddedpayments.auth.infrastructure.repository.UserAccountJpaRepository;
import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantJpaRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.util.List;
import org.springframework.stereotype.Service;

import java.util.UUID;
import org.springframework.http.HttpStatus;

@Service
public class GetCurrentMerchantUseCase {

    private final MerchantJpaRepository merchantRepository;
    private final UserAccountJpaRepository userAccountRepository;

    public GetCurrentMerchantUseCase(MerchantJpaRepository merchantRepository,
                                     UserAccountJpaRepository userAccountRepository) {
        this.merchantRepository = merchantRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public CurrentMerchantResponse execute(UUID currentUserId, String role, UUID merchantId) {
        if ("ROLE_MERCHANT".equals(role)) {
            UUID resolvedMerchantId = merchantId != null ? merchantId : currentUserId;
            Merchant merchant = merchantRepository.findById(resolvedMerchantId)
                    .orElseThrow(() -> new DomainException(
                            HttpStatus.NOT_FOUND,
                            "MERCHANT_NOT_FOUND",
                            "Merchant not found",
                            List.of("merchantId: " + resolvedMerchantId)
                    ));
            return new CurrentMerchantResponse(
                    merchant.getId(),
                    merchant.getEmail(),
                    merchant.getStatus(),
                    role,
                    merchant.getId()
            );
        }

        UserAccount user = userAccountRepository.findById(currentUserId)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found",
                        List.of("userId: " + currentUserId)
                ));

        UUID resolvedMerchantId = merchantId;
        if ("ROLE_ADMIN".equals(role) && resolvedMerchantId == null) {
            resolvedMerchantId = merchantRepository.findByEmail(user.getEmail())
                    .map(Merchant::getId)
                    .orElse(null);
        }

        return new CurrentMerchantResponse(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                role,
                resolvedMerchantId
        );
    }

    public record CurrentMerchantResponse(UUID id, String email, String status, String role, UUID merchantId) {
    }
}
