package com.paymentplatform.embeddedpayments.merchant.api;

import com.paymentplatform.embeddedpayments.merchant.api.dto.ChangeMerchantStatusRequest;
import com.paymentplatform.embeddedpayments.merchant.api.dto.MerchantDetailResponse;
import com.paymentplatform.embeddedpayments.merchant.api.dto.MerchantUpdateResponse;
import com.paymentplatform.embeddedpayments.merchant.api.dto.RegisterBankAccountRequest;
import com.paymentplatform.embeddedpayments.merchant.api.dto.UpdateMerchantContactRequest;
import com.paymentplatform.embeddedpayments.merchant.application.ActivateMerchantUseCase;
import com.paymentplatform.embeddedpayments.merchant.application.DeactivateMerchantUseCase;
import com.paymentplatform.embeddedpayments.merchant.application.GetMerchantDetailUseCase;
import com.paymentplatform.embeddedpayments.merchant.application.GetMerchantDetailUseCase.MerchantDetail;
import com.paymentplatform.embeddedpayments.merchant.application.RegisterBankAccountUseCase;
import com.paymentplatform.embeddedpayments.merchant.application.RegisterMerchantUseCase;
import com.paymentplatform.embeddedpayments.merchant.application.UpdateMerchantContactUseCase;
import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final RegisterMerchantUseCase registerMerchantUseCase;
    private final UpdateMerchantContactUseCase updateMerchantContactUseCase;
    private final RegisterBankAccountUseCase registerBankAccountUseCase;
    private final ActivateMerchantUseCase activateMerchantUseCase;
    private final DeactivateMerchantUseCase deactivateMerchantUseCase;
    private final GetMerchantDetailUseCase getMerchantDetailUseCase;
    private final AuthenticationService authenticationService;

    public MerchantController(RegisterMerchantUseCase registerMerchantUseCase,
                              UpdateMerchantContactUseCase updateMerchantContactUseCase,
                              RegisterBankAccountUseCase registerBankAccountUseCase,
                              ActivateMerchantUseCase activateMerchantUseCase,
                              DeactivateMerchantUseCase deactivateMerchantUseCase,
                              GetMerchantDetailUseCase getMerchantDetailUseCase,
                              AuthenticationService authenticationService) {
        this.registerMerchantUseCase = registerMerchantUseCase;
        this.updateMerchantContactUseCase = updateMerchantContactUseCase;
        this.registerBankAccountUseCase = registerBankAccountUseCase;
        this.activateMerchantUseCase = activateMerchantUseCase;
        this.deactivateMerchantUseCase = deactivateMerchantUseCase;
        this.getMerchantDetailUseCase = getMerchantDetailUseCase;
        this.authenticationService = authenticationService;
    }

    /**
     * POST /api/v1/merchants - Registrar nuevo comercio
     */
    @PostMapping
    public ResponseEntity<MerchantUpdateResponse> register(@Valid @RequestBody RegisterMerchantRequest request) {
        RegisterMerchantUseCase.RegisteredMerchant registeredMerchant = registerMerchantUseCase.execute(request.name(), request.email());
        Merchant merchant = registeredMerchant.merchant();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MerchantUpdateResponse(
                        merchant.getId(),
                        merchant.getName(),
                        merchant.getEmail(),
                        merchant.getStatus(),
                        merchant.getUpdatedAt(),
                        "Merchant registered successfully",
                        registeredMerchant.apiKey()
                ));
    }

    /**
     * HU 1.2: PUT /api/v1/merchants/{id}/contact - Actualizar información de contacto
     */
    @PutMapping("/{id}/contact")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MerchantUpdateResponse> updateContact(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMerchantContactRequest request) {
        
        String actor = authenticationService.getCurrentUser();
        Merchant updatedMerchant = updateMerchantContactUseCase.execute(
                id,
                request.contactName(),
                request.contactEmail(),
                actor
        );

        return ResponseEntity.ok(new MerchantUpdateResponse(
                updatedMerchant.getId(),
                updatedMerchant.getName(),
                updatedMerchant.getEmail(),
                updatedMerchant.getStatus(),
                updatedMerchant.getUpdatedAt(),
                "Contact information updated successfully",
                null
        ));
    }

    /**
     * HU 1.3: PUT /api/v1/merchants/{id}/bank-account - Registrar datos bancarios
     */
    @PutMapping("/{id}/bank-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MerchantUpdateResponse> registerBankAccount(
            @PathVariable UUID id,
            @Valid @RequestBody RegisterBankAccountRequest request) {
        
        String actor = authenticationService.getCurrentUser();
        Merchant updatedMerchant = registerBankAccountUseCase.execute(
                id,
                request.iban(),
                request.routingNumber(),
                request.accountHolderName(),
                actor
        );

        return ResponseEntity.ok(new MerchantUpdateResponse(
                updatedMerchant.getId(),
                updatedMerchant.getName(),
                updatedMerchant.getEmail(),
                updatedMerchant.getStatus(),
                updatedMerchant.getUpdatedAt(),
                "Bank account registered successfully",
                null
        ));
    }

    /**
     * HU 1.4: PATCH /api/v1/merchants/{id}/activate - Activar comercio (Admin only)
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantUpdateResponse> activateMerchant(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeMerchantStatusRequest request) {
        
        String actor = authenticationService.getCurrentUser();
        Merchant updatedMerchant = activateMerchantUseCase.execute(
                id,
                request.reason(),
                actor
        );

        return ResponseEntity.ok(new MerchantUpdateResponse(
                updatedMerchant.getId(),
                updatedMerchant.getName(),
                updatedMerchant.getEmail(),
                updatedMerchant.getStatus(),
                updatedMerchant.getUpdatedAt(),
                "Merchant activated successfully",
                null
        ));
    }

    /**
     * HU 1.5: PATCH /api/v1/merchants/{id}/deactivate - Desactivar comercio (Admin only)
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantUpdateResponse> deactivateMerchant(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeMerchantStatusRequest request) {
        
        String actor = authenticationService.getCurrentUser();
        Merchant updatedMerchant = deactivateMerchantUseCase.execute(
                id,
                request.reason(),
                actor
        );

        return ResponseEntity.ok(new MerchantUpdateResponse(
                updatedMerchant.getId(),
                updatedMerchant.getName(),
                updatedMerchant.getEmail(),
                updatedMerchant.getStatus(),
                updatedMerchant.getUpdatedAt(),
                "Merchant deactivated successfully",
                null
        ));
    }

    /**
     * HU 1.6: GET /api/v1/merchants/{id} - Obtener detalles del comercio con enmascaramiento
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MerchantDetailResponse> getMerchantDetail(@PathVariable UUID id) {
        String requesterRole = authenticationService.getCurrentUserRole();
        String requesterId = authenticationService.getCurrentUser();

        MerchantDetail detail = getMerchantDetailUseCase.execute(id, requesterRole, requesterId);

        return ResponseEntity.ok(new MerchantDetailResponse(
                detail.id(),
                detail.name(),
                detail.email(),
                detail.contactName(),
                detail.contactEmail(),
                detail.status(),
                detail.bankAccountData(),
                detail.updatedAt()
        ));
    }

    public record RegisterMerchantRequest(
            @NotBlank String name,
            @NotBlank @Email String email) {
    }
}
