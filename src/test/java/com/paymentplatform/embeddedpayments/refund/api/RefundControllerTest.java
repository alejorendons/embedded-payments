package com.paymentplatform.embeddedpayments.refund.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentplatform.embeddedpayments.refund.application.CreateRefundUseCase;
import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
import java.math.BigDecimal;
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
class RefundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateRefundUseCase createRefundUseCase;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser(roles = "API_CLIENT")
    void create_returns201_whenMerchantIdPresent() throws Exception {
        UUID merchantId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        when(authenticationService.getCurrentMerchantId()).thenReturn(merchantId);
        when(createRefundUseCase.execute(eq(merchantId), eq(txId), any(BigDecimal.class), eq("motivo")))
                .thenReturn(new Refund(refundId, txId, new BigDecimal("10.00"), "motivo", "PENDING"));

        mockMvc.perform(post("/api/v1/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefundController.CreateRefundRequest(
                                txId, new BigDecimal("10.00"), "motivo"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(refundId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "API_CLIENT")
    void create_returns401_whenMerchantIdMissing() throws Exception {
        when(authenticationService.getCurrentMerchantId()).thenReturn(null);

        mockMvc.perform(post("/api/v1/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefundController.CreateRefundRequest(
                                UUID.randomUUID(), new BigDecimal("10.00"), "x"))))
                .andExpect(status().isUnauthorized());
    }
}
