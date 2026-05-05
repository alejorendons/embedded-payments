package com.paymentplatform.embeddedpayments.merchant.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO para actualizar información de contacto (HU 1.2)
 */
public record UpdateMerchantContactRequest(
        @NotBlank(message = "Contact name cannot be blank")
        @JsonProperty("contact_name")
        String contactName,

        @Email(message = "Contact email must be valid")
        @JsonProperty("contact_email")
        String contactEmail
) {
}

