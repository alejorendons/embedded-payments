package com.paymentplatform.embeddedpayments.merchant.api;

import com.paymentplatform.embeddedpayments.shared.audit.AuditEvent;
import com.paymentplatform.embeddedpayments.shared.audit.AuditEventRepository;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("HU 1.1 – Criterios de Aceptación: Registro de Comercio")
class HU11_RegisterMerchantAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditEventRepository auditEventRepository;

    // ── CA-01 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CA-01: Registro exitoso por comercio potencial → 201, estado INACTIVE, api_key con prefijo epk_")
    void CA01_registroPorComercioPotencial_retorna201_estadoINACTIVE_conApiKey() throws Exception {
        String body = """
                {
                  "name": "Comercio Potencial",
                  "email": "comercio+ca01@hu11.test"
                }
                """;

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.api_key").value(startsWith("epk_")));
    }

    // ── CA-02 ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@hu11.test", roles = "ADMIN")
    @DisplayName("CA-02: Registro exitoso por administrador → 201, estado INACTIVE")
    void CA02_registroPorAdministrador_retorna201_estadoINACTIVE() throws Exception {
        String body = """
                {
                  "name": "Comercio Admin",
                  "email": "comercio+ca02@hu11.test"
                }
                """;

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    // ── CA-03 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CA-03: Payload inválido → 400 con cuerpo uniforme (errorCode, message, details, traceId)")
    void CA03_payloadInvalido_retorna400_conCuerpoDeErrorEstandar() throws Exception {
        String invalidBody = """
                {
                  "name": "",
                  "email": "correo-invalido"
                }
                """;

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    // ── CA-04 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CA-04: Registro duplicado → 409 con cuerpo uniforme (errorCode, message, traceId)")
    void CA04_registroDuplicado_retorna409_conCuerpoDeErrorEstandar() throws Exception {
        String body = """
                {
                  "name": "Duplicado",
                  "email": "duplicado+ca04@hu11.test"
                }
                """;

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("MERCHANT_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    // ── CA-05 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CA-05: Registro exitoso genera evento de auditoría con origen y timestamp")
    void CA05_registroExitoso_generaEventoDeAuditoria_conOrigenYTimestamp() throws Exception {
        String body = """
                {
                  "name": "Comercio Auditoria",
                  "email": "auditoria+ca05@hu11.test"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        UUID merchantId = extractIdFromResponse(result.getResponse().getContentAsString());

        AuditEvent event = auditEventRepository.findAll().stream()
                .filter(e -> "merchant_registered".equals(e.getEventType())
                        && merchantId.equals(e.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontró evento de auditoría 'merchant_registered' para merchantId: " + merchantId));

        assertThat(event.getEntityType()).isEqualTo("merchant");
        assertThat(event.getOrigin()).isIn("SELF_REGISTRATION", "ADMIN_REGISTRATION");
        assertThat(event.getHappenedAt()).isNotNull();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UUID extractIdFromResponse(String json) {
        Matcher matcher = Pattern.compile("\"id\":\"([0-9a-fA-F-]{36})\"").matcher(json);
        assertThat(matcher.find()).isTrue();
        return UUID.fromString(matcher.group(1));
    }
}
