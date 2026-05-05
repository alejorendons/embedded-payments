package com.paymentplatform.embeddedpayments.payment.api;

import com.paymentplatform.embeddedpayments.payment.application.CreatePaymentIntentUseCase;
import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
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
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final CreatePaymentIntentUseCase createPaymentIntentUseCase;
    private final AuthenticationService authenticationService;

    public PaymentController(CreatePaymentIntentUseCase createPaymentIntentUseCase,
                             AuthenticationService authenticationService) {
        this.createPaymentIntentUseCase = createPaymentIntentUseCase;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/intents")
    public ResponseEntity<PaymentIntentResponse> createIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        UUID merchantId = authenticationService.getCurrentMerchantId();
        if (merchantId == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid API key", List.of());
        }

        PaymentIntent intent = createPaymentIntentUseCase.execute(
                merchantId,
                request.amount(),
                request.currency()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PaymentIntentResponse(
                        intent.getId(),
                        intent.getMerchantId(),
                        intent.getAmount(),
                        intent.getCurrency(),
                        intent.getStatus()
                ));
    }

    public record CreatePaymentIntentRequest(
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank String currency
    ) {
    }

    public record PaymentIntentResponse(UUID id, UUID merchantId, BigDecimal amount, String currency, String status) {
    }
}
