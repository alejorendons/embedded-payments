package com.paymentplatform.embeddedpayments.payment.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentplatform.embeddedpayments.payment.application.CreatePaymentIntentUseCase;
import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreatePaymentIntentUseCase createPaymentIntentUseCase;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser(roles = "API_CLIENT")
    void debeRetornar201CuandoRequestEsValido() throws Exception {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        UUID intentId = UUID.randomUUID();
        PaymentController.CreatePaymentIntentRequest requestBody =
                new PaymentController.CreatePaymentIntentRequest(new BigDecimal("100.50"), "USD");

        PaymentIntent mockIntent = new PaymentIntent(
                intentId, merchantId, requestBody.amount(), requestBody.currency(), "CREATED");

        when(authenticationService.getCurrentMerchantId()).thenReturn(merchantId);
        when(createPaymentIntentUseCase.execute(
                eq(merchantId), eq(requestBody.amount()), eq(requestBody.currency())))
                .thenReturn(mockIntent);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(intentId.toString()))
                .andExpect(jsonPath("$.merchantId").value(merchantId.toString()))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @WithMockUser(roles = "API_CLIENT")
    void debeRetornar401CuandoNoHayMerchantId() throws Exception {
        // Arrange
        PaymentController.CreatePaymentIntentRequest requestBody =
                new PaymentController.CreatePaymentIntentRequest(new BigDecimal("100.50"), "USD");

        when(authenticationService.getCurrentMerchantId()).thenReturn(null);

        // Act & Assert — el controller lanza DomainException(UNAUTHORIZED) cuando merchantId es null
        mockMvc.perform(post("/api/v1/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "API_CLIENT")
    void debeRetornar400CuandoAmountEsNegativo() throws Exception {
        // Arrange — @DecimalMin("0.01") en el record debería rechazar valores negativos
        PaymentController.CreatePaymentIntentRequest requestBody =
                new PaymentController.CreatePaymentIntentRequest(new BigDecimal("-10.00"), "USD");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "API_CLIENT")
    void debeRetornar400CuandoCurrencyEsBlank() throws Exception {
        // Arrange — @NotBlank en el record rechaza currency vacío
        PaymentController.CreatePaymentIntentRequest requestBody =
                new PaymentController.CreatePaymentIntentRequest(new BigDecimal("50.00"), "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }
}
