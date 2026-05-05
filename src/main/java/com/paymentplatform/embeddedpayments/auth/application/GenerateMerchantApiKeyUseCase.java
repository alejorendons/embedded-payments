package com.paymentplatform.embeddedpayments.auth.application;

import com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey;
import com.paymentplatform.embeddedpayments.auth.domain.repository.MerchantApiKeyRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class GenerateMerchantApiKeyUseCase {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MerchantApiKeyRepository merchantApiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    public GenerateMerchantApiKeyUseCase(MerchantApiKeyRepository merchantApiKeyRepository,
                                         PasswordEncoder passwordEncoder) {
        this.merchantApiKeyRepository = merchantApiKeyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String execute(UUID merchantId) {
        UUID keyId = UUID.randomUUID();
        String rawKey = buildRawKey(keyId);

        MerchantApiKey key = new MerchantApiKey(
                keyId,
                merchantId,
                passwordEncoder.encode(rawKey),
                "ACTIVE",
                Instant.now()
        );

        merchantApiKeyRepository.save(key);
        return rawKey;
    }

    private String buildRawKey(UUID keyId) {
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return "epk_" + keyId + "_" + secret;
    }
}
