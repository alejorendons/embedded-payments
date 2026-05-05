package com.paymentplatform.embeddedpayments.merchant.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO para respuesta de detalles de comercio (HU 1.6)
 */
public record MerchantDetailResponse(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("name")
        String name,

        @JsonProperty("email")
        String email,

        @JsonProperty("contact_name")
        String contactName,

        @JsonProperty("contact_email")
        String contactEmail,

        @JsonProperty("status")
        String status,

        @JsonProperty("bank_account_data")
        String bankAccountData,

        @JsonProperty("updated_at")
        Instant updatedAt
) {
}

