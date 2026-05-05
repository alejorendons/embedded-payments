package com.paymentplatform.embeddedpayments.merchant.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para cambiar estado de comercio (activar/desactivar)
 */
public record ChangeMerchantStatusRequest(
        @JsonProperty("reason")
        String reason
) {
}

