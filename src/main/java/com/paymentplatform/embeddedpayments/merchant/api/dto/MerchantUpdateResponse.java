package com.paymentplatform.embeddedpayments.merchant.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO genérico para respuestas de actualización de comercio
 */
public record MerchantUpdateResponse(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("name")
        String name,

        @JsonProperty("email")
        String email,

        @JsonProperty("status")
        String status,

        @JsonProperty("updated_at")
        Instant updatedAt,

        @JsonProperty("message")
        String message,

        @JsonProperty("api_key")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String apiKey
) {
}
