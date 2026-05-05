package com.paymentplatform.embeddedpayments.merchant.application;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.exception.MerchantNotFoundException;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantAuditDetail;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantAuditDetailJpaRepository;
import com.paymentplatform.embeddedpayments.shared.audit.AuditEventService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU 1.2: Actualización de información de contacto del comercio
 * Como Comercio autenticado, quiero actualizar mi información básica de contacto
 * (nombre y email), para mantener mis datos operativos actualizados.
 */
@Service
public class UpdateMerchantContactUseCase {

    private final MerchantRepository merchantRepository;
    private final MerchantAuditDetailJpaRepository auditDetailRepository;
    private final AuditEventService auditEventService;

    public UpdateMerchantContactUseCase(MerchantRepository merchantRepository,
                                        MerchantAuditDetailJpaRepository auditDetailRepository,
                                        AuditEventService auditEventService) {
        this.merchantRepository = merchantRepository;
        this.auditDetailRepository = auditDetailRepository;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public Merchant execute(UUID merchantId, String contactName, String contactEmail, String actor) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant not found with id: " + merchantId
                ));

        // Registrar cambios en auditoría
        if (contactName != null && !contactName.equals(merchant.getContactName())) {
            auditDetailRepository.save(new MerchantAuditDetail(
                    merchantId,
                    "MERCHANT_CONTACT_UPDATED",
                    "contact_name",
                    merchant.getContactName(),
                    contactName,
                    actor
            ));
        }

        if (contactEmail != null && !contactEmail.equals(merchant.getContactEmail())) {
            auditDetailRepository.save(new MerchantAuditDetail(
                    merchantId,
                    "MERCHANT_CONTACT_UPDATED",
                    "contact_email",
                    merchant.getContactEmail(),
                    contactEmail,
                    actor
            ));
        }

        // Actualizar el comercio
        merchant.updateContact(contactName, contactEmail);
        Merchant updatedMerchant = merchantRepository.save(merchant);

        // Registrar evento en auditoría
        auditEventService.registerMerchantContactUpdated(merchantId, actor);

        return updatedMerchant;
    }
}

