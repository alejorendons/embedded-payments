package com.paymentplatform.embeddedpayments.merchant.application;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.exception.MerchantNotFoundException;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantStatusHistory;
import com.paymentplatform.embeddedpayments.merchant.infrastructure.persistence.MerchantStatusHistoryJpaRepository;
import com.paymentplatform.embeddedpayments.shared.audit.AuditEventService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU 1.5: Desactivación de comercio
 * Como Administrador, quiero desactivar un comercio, para suspender su operación por
 * seguridad o decisión comercial.
 */
@Service
public class DeactivateMerchantUseCase {

    private final MerchantRepository merchantRepository;
    private final MerchantStatusHistoryJpaRepository statusHistoryRepository;
    private final AuditEventService auditEventService;

    public DeactivateMerchantUseCase(MerchantRepository merchantRepository,
                                     MerchantStatusHistoryJpaRepository statusHistoryRepository,
                                     AuditEventService auditEventService) {
        this.merchantRepository = merchantRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public Merchant execute(UUID merchantId, String reason, String actor) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant not found with id: " + merchantId
                ));

        String previousStatus = merchant.getStatus();
        
        // Cambiar estado a INACTIVE (valida transiciones)
        merchant.changeStatus("INACTIVE");

        // Guardar historial de cambio de estado
        statusHistoryRepository.save(new MerchantStatusHistory(
                merchantId,
                previousStatus,
                "INACTIVE",
                actor,
                reason
        ));

        // Guardar el comercio
        Merchant updatedMerchant = merchantRepository.save(merchant);

        // Registrar en auditoría y bloquear nuevas transacciones
        auditEventService.registerMerchantDeactivated(merchantId, actor);

        return updatedMerchant;
    }
}

