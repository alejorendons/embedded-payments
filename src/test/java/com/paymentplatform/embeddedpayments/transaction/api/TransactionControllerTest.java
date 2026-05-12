package com.paymentplatform.embeddedpayments.transaction.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
import com.paymentplatform.embeddedpayments.transaction.application.CreateTransactionUseCase;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser(roles = "API_CLIENT")
    void create_returns201_whenValid() throws Exception {
        UUID merchantId = UUID.randomUUID();
        UUID intentId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        when(authenticationService.getCurrentMerchantId()).thenReturn(merchantId);
        when(createTransactionUseCase.execute(eq(merchantId), eq(intentId), any(BigDecimal.class)))
                .thenReturn(new PaymentTransaction(txId, intentId, new BigDecimal("25.00"), "SUCCEEDED", Instant.now()));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TransactionController.CreateTransactionRequest(intentId, new BigDecimal("25.00")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(txId.toString()))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));
    }

    @Test
    @WithMockUser(roles = "API_CLIENT")
    void create_returns401_whenMerchantIdMissing() throws Exception {
        when(authenticationService.getCurrentMerchantId()).thenReturn(null);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TransactionController.CreateTransactionRequest(
                                        UUID.randomUUID(), new BigDecimal("25.00")))))
                .andExpect(status().isUnauthorized());
    }
}
