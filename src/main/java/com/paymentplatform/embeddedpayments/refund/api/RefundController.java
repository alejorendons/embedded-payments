package com.paymentplatform.embeddedpayments.refund.api;

import com.paymentplatform.embeddedpayments.refund.application.CreateRefundUseCase;
import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/refunds")
public class RefundController {

    private final CreateRefundUseCase createRefundUseCase;
    private final AuthenticationService authenticationService;

    public RefundController(CreateRefundUseCase createRefundUseCase,
                            AuthenticationService authenticationService) {
        this.createRefundUseCase = createRefundUseCase;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<RefundResponse> create(@Valid @RequestBody CreateRefundRequest request) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        Refund refund = createRefundUseCase.execute(merchantId, request.transactionId(), request.amount(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RefundResponse(
                        refund.getId(),
                        refund.getTransactionId(),
                        refund.getAmount(),
                        refund.getReason(),
                        refund.getStatus()
                ));
    }

    public record CreateRefundRequest(@NotNull UUID transactionId,
                                      @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                                      @NotBlank String reason) {
    }

    public record RefundResponse(UUID id, UUID transactionId, BigDecimal amount, String reason, String status) {
    }
}
