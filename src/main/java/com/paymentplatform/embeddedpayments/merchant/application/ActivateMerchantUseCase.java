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
 * HU 1.4: Activación de comercio
 * Como Administrador, quiero activar un comercio, para habilitar su operación en la plataforma.
 */
@Service
public class ActivateMerchantUseCase {

    private final MerchantRepository merchantRepository;
    private final MerchantStatusHistoryJpaRepository statusHistoryRepository;
    private final AuditEventService auditEventService;

    public ActivateMerchantUseCase(MerchantRepository merchantRepository,
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
        
        // Cambiar estado (valida transiciones)
        merchant.changeStatus("ACTIVE");

        // Guardar historial de cambio de estado
        statusHistoryRepository.save(new MerchantStatusHistory(
                merchantId,
                previousStatus,
                "ACTIVE",
                actor,
                reason
        ));

        // Guardar el comercio
        Merchant updatedMerchant = merchantRepository.save(merchant);

        // Registrar en auditoría
        auditEventService.registerMerchantActivated(merchantId, actor);

        return updatedMerchant;
    }
}

