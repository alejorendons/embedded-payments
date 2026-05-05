package com.paymentplatform.embeddedpayments.transaction.api;

import com.paymentplatform.embeddedpayments.transaction.application.CreateTransactionUseCase;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final AuthenticationService authenticationService;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                 AuthenticationService authenticationService) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        PaymentTransaction transaction = createTransactionUseCase.execute(merchantId, request.paymentIntentId(), request.amount());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TransactionResponse(
                        transaction.getId(),
                        transaction.getPaymentIntentId(),
                        transaction.getAmount(),
                        transaction.getStatus(),
                        transaction.getCreatedAt()
                ));
    }

    public record CreateTransactionRequest(@NotNull UUID paymentIntentId,
                                           @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
    }

    public record TransactionResponse(UUID id, UUID paymentIntentId, BigDecimal amount, String status, Instant createdAt) {
    }
}
