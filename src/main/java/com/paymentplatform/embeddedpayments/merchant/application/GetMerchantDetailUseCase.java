package com.paymentplatform.embeddedpayments.merchant.application;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.exception.MerchantNotFoundException;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.shared.security.DataMaskingService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU 1.6: Consulta individual de comercio por ID
 * Como Administrador, quiero consultar un comercio por su ID, para auditar su estado
 * e información. Implementa enmascaramiento de datos sensibles según OWASP.
 */
@Service
@Transactional(readOnly = true)
public class GetMerchantDetailUseCase {

    private final MerchantRepository merchantRepository;
    private final DataMaskingService dataMaskingService;

    public GetMerchantDetailUseCase(MerchantRepository merchantRepository,
                                     DataMaskingService dataMaskingService) {
        this.merchantRepository = merchantRepository;
        this.dataMaskingService = dataMaskingService;
    }

    /**
     * Obtiene detalles de un comercio con enmascaramiento de datos sensibles
     * @param merchantId ID del comercio
     * @param requesterRole Rol del solicitante (ADMIN, MERCHANT, etc.)
     * @param requesterId ID del solicitante (para validar ownership)
     * @return Merchant con datos enmascarados según permisos
     */
    public MerchantDetail execute(UUID merchantId, String requesterRole, String requesterId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(
                        "Merchant not found with id: " + merchantId
                ));

        return buildMaskedDetail(merchant, requesterRole, requesterId);
    }

    private MerchantDetail buildMaskedDetail(Merchant merchant, String requesterRole, String requesterId) {
        boolean isOwner = merchant.getId().toString().equals(requesterId);
        boolean isAdmin = "ROLE_ADMIN".equals(requesterRole);

        // Enmascarar email de contacto según OWASP (no mostrar a otros comercios)
        String maskedContactEmail = merchant.getContactEmail();
        if (!isOwner && !isAdmin && maskedContactEmail != null) {
            maskedContactEmail = dataMaskingService.maskEmail(maskedContactEmail);
        }

        // Datos bancarios: solo owner y admin pueden ver (cifrado desencriptado)
        String maskedBankData = null;
        if ((isOwner || isAdmin) && merchant.getBankAccountEncrypted() != null) {
            maskedBankData = "[ENCRYPTED - Access granted]";
        } else if (merchant.getBankAccountEncrypted() != null) {
            maskedBankData = "[RESTRICTED]";
        }

        return new MerchantDetail(
                merchant.getId(),
                merchant.getName(),
                merchant.getEmail(),
                merchant.getContactName(),
                maskedContactEmail,
                merchant.getStatus(),
                maskedBankData,
                merchant.getUpdatedAt()
        );
    }

    public record MerchantDetail(
            UUID id,
            String name,
            String email,
            String contactName,
            String contactEmail,
            String status,
            String bankAccountData,
            java.time.Instant updatedAt
    ) {
    }
}

