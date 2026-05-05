package com.paymentplatform.embeddedpayments.shared.audit;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void registerMerchantCreated(UUID merchantId, String origin, String actor) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                "merchant_registered",
                "merchant",
                merchantId,
                origin,
                actor,
                Instant.now()
        );
        auditEventRepository.save(event);
    }

    /**
     * HU 1.2: Registra actualización de información de contacto
     */
    public void registerMerchantContactUpdated(UUID merchantId, String actor) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                "merchant_contact_updated",
                "merchant",
                merchantId,
                "SELF_UPDATE",
                actor,
                Instant.now()
        );
        auditEventRepository.save(event);
    }

    /**
     * HU 1.3: Registra evento crítico de registro de cuenta bancaria
     */
    public void registerBankAccountRegistered(UUID merchantId, String actor) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                "merchant_bank_account_registered",
                "merchant",
                merchantId,
                "CRITICAL_EVENT",
                actor,
                Instant.now()
        );
        auditEventRepository.save(event);
    }

    /**
     * HU 1.4: Registra activación de comercio
     */
    public void registerMerchantActivated(UUID merchantId, String actor) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                "merchant_activated",
                "merchant",
                merchantId,
                "ADMIN_ACTION",
                actor,
                Instant.now()
        );
        auditEventRepository.save(event);
    }

    /**
     * HU 1.5: Registra desactivación de comercio
     */
    public void registerMerchantDeactivated(UUID merchantId, String actor) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                "merchant_deactivated",
                "merchant",
                merchantId,
                "ADMIN_ACTION",
                actor,
                Instant.now()
        );
        auditEventRepository.save(event);
    }

    /**
     * HU 1.6: Registra consulta de detalles de comercio
     */
    public void registerMerchantDetailsQueried(UUID merchantId, String actor) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                "merchant_details_queried",
                "merchant",
                merchantId,
                "READ_ACCESS",
                actor,
                Instant.now()
        );
        auditEventRepository.save(event);
    }
}

