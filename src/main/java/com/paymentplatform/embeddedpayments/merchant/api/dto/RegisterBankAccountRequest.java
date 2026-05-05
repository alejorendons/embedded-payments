package com.paymentplatform.embeddedpayments.merchant.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paymentplatform.embeddedpayments.shared.validation.ValidIBAN;
import com.paymentplatform.embeddedpayments.shared.validation.ValidRoutingNumber;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para registrar datos bancarios (HU 1.3)
 */
public record RegisterBankAccountRequest(
        @ValidIBAN(message = "Invalid IBAN format or checksum")
        @JsonProperty("iban")
        String iban,

        @ValidRoutingNumber(message = "Invalid routing number format")
        @JsonProperty("routing_number")
        String routingNumber,

        @NotBlank(message = "Account holder name cannot be blank")
        @JsonProperty("account_holder_name")
        String accountHolderName
) {
}

