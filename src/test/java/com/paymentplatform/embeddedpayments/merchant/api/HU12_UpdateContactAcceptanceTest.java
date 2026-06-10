package com.paymentplatform.embeddedpayments.merchant.api;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.shared.audit.AuditEvent;
import com.paymentplatform.embeddedpayments.shared.audit.AuditEventRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Criterios de aceptación HU 1.2 – Actualización de Información de Contacto.
 *
 * Escenarios comprometidos:
 *   CA-01: Actualización exitosa con datos válidos → 200.
 *   CA-02: contact_name vacío → 400 Bad Request.
 *   CA-03: contact_email con formato inválido → 400 Bad Request.
 *   CA-04: Comercio no existe → 404 Not Found.
 *   CA-05: Sin autenticación → 403 Forbidden.
 *   CA-06: Actualización exitosa genera evento de auditoría con timestamp.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("HU 1.2 – Criterios de Aceptación: Actualización de Información de Contacto")
class HU12_UpdateContactAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private static final String VALID_CONTACT_BODY = """
            {
              "contact_name": "Ana García",
              "contact_email": "ana.garcia@example.com"
            }
            """;

    // ── setup helper ──────────────────────────────────────────────────────────

    private Merchant crearComercio(String estado) {
        String email = "m-" + UUID.randomUUID() + "@hu12.test";
        Merchant merchant = new Merchant(UUID.randomUUID(), "Comercio HU12", email, estado);
        return merchantRepository.save(merchant);
    }

    // ── CA-01 ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MERCHANT")
    @DisplayName("CA-01: Datos válidos → 200 con id del comercio y mensaje de éxito")
    void CA01_datosValidos_retorna200_conMensajeExito() throws Exception {
        Merchant m = crearComercio("ACTIVE");

        mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CONTACT_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(m.getId().toString()))
                .andExpect(jsonPath("$.message").value("Contact information updated successfully"));
    }

    // ── CA-02 ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MERCHANT")
    @DisplayName("CA-02: contact_name vacío → 400 con cuerpo uniforme (errorCode, message, details, traceId)")
    void CA02_contactNameVacio_retorna400_conCuerpoDeErrorEstandar() throws Exception {
        Merchant m = crearComercio("ACTIVE");

        mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contact_name": "",
                                  "contact_email": "valido@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    // ── CA-03 ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MERCHANT")
    @DisplayName("CA-03: contact_email inválido → 400 con cuerpo uniforme (errorCode, message, details, traceId)")
    void CA03_emailInvalido_retorna400_conCuerpoDeErrorEstandar() throws Exception {
        Merchant m = crearComercio("ACTIVE");

        mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contact_name": "Nombre Válido",
                                  "contact_email": "no-es-un-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    // ── CA-04 ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MERCHANT")
    @DisplayName("CA-04: Comercio no existe → 404 con cuerpo uniforme (errorCode, message, traceId)")
    void CA04_comercioNoExiste_retorna404_conCuerpoDeErrorEstandar() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/merchants/{id}/contact", idInexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CONTACT_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("MERCHANT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    // ── CA-05 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CA-05: Sin autenticación → 403 Forbidden")
    void CA05_sinAutenticacion_retornaForbidden() throws Exception {
        Merchant m = crearComercio("ACTIVE");

        mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CONTACT_BODY))
                .andExpect(status().isForbidden());
    }

    // ── CA-06 ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MERCHANT")
    @DisplayName("CA-06: Actualización exitosa genera evento 'merchant_contact_updated' con timestamp")
    void CA06_actualizacionExitosa_generaEventoDeAuditoria_conTimestamp() throws Exception {
        Merchant m = crearComercio("ACTIVE");

        mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CONTACT_BODY))
                .andExpect(status().isOk());

        AuditEvent event = auditEventRepository.findAll().stream()
                .filter(e -> "merchant_contact_updated".equals(e.getEventType())
                        && m.getId().equals(e.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontró evento de auditoría 'merchant_contact_updated' para merchantId: " + m.getId()));

        assertThat(event.getEntityType()).isEqualTo("merchant");
        assertThat(event.getHappenedAt()).isNotNull();
    }
}
