package com.paymentplatform.embeddedpayments.merchant.application;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.exception.MerchantNotFoundException;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantAuditDetail;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantAuditDetailJpaRepository;
import com.paymentplatform.embeddedpayments.shared.audit.AuditEventService;
import com.paymentplatform.embeddedpayments.shared.security.EncryptionService;
import java.security.MessageDigest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU 1.3: Actualización de información financiera del comercio
 * Como Comercio autenticado y activo, quiero registrar o actualizar mis datos bancarios,
 * para asegurar la correcta liquidación de pagos.
 */
@Service
public class RegisterBankAccountUseCase {

    private final MerchantRepository merchantRepository;
    private final MerchantAuditDetailJpaRepository auditDetailRepository;
    private final AuditEventService auditEventService;
    private final EncryptionService encryptionService;

    public RegisterBankAccountUseCase(MerchantRepository merchantRepository,
                                      MerchantAuditDetailJpaRepository auditDetailRepository,
                                      AuditEventService auditEventService,
                                      EncryptionService encryptionService) {
        this.merchantRepository = merchantRepository;
        this.auditDetailRepository = auditDetailRepository;
        this.auditEventService = auditEventService;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public Merchant execute(UUID merchantId, String iban, String routingNumber, 
                           String accountHolderName, String actor) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant not found with id: " + merchantId
                ));

        // Validar que el comercio esté activo (HU 1.3 criterio: solo activos)
        if (!merchant.isActive()) {
            throw new com.paymentplatform.embeddedpayments.merchant.domain.exception.MerchantInactiveException(
                    "Cannot register bank account for inactive merchant"
            );
        }

        // Encriptar datos bancarios (AES-256-GCM) - una sola vez
        String accountData = String.format("%s|%s|%s", iban, routingNumber, accountHolderName);
        byte[] encryptedDataBytes = encryptionService.encryptToBytes(accountData);
        String encryptedData = java.util.Base64.getEncoder().encodeToString(encryptedDataBytes);
        
        // Generar hash de IBAN para búsquedas sin desencriptar
        String ibanHash = hashValue(iban);

        // Registrar cambio en auditoría (evento crítico)
        String previousBankData = merchant.getBankAccountEncrypted();
        if (previousBankData == null) {
            auditDetailRepository.save(new MerchantAuditDetail(
                    merchantId,
                    "MERCHANT_BANK_ACCOUNT_REGISTERED",
                    "bank_account_encrypted",
                    null,
                    "[ENCRYPTED_BANK_DATA]",
                    actor
            ));
        } else {
            auditDetailRepository.save(new MerchantAuditDetail(
                    merchantId,
                    "MERCHANT_BANK_ACCOUNT_UPDATED",
                    "bank_account_encrypted",
                    "[ENCRYPTED_BANK_DATA]",
                    "[ENCRYPTED_BANK_DATA]",
                    actor
            ));
        }

        // Actualizar el comercio
        merchant.registerBankAccount(encryptedData, ibanHash, encryptedDataBytes);
        Merchant updatedMerchant = merchantRepository.save(merchant);

        // Registrar evento crítico en auditoría
        auditEventService.registerBankAccountRegistered(merchantId, actor);

        return updatedMerchant;
    }

    private String hashValue(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes());
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}

