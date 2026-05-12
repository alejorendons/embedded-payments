package com.paymentplatform.embeddedpayments.merchant.api;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de API para CU-02 … CU-06 (HU 1.2 – 1.6): contacto, cuenta bancaria,
 * activar/desactivar y consulta de comercio.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MerchantManagementApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MerchantRepository merchantRepository;

    private Merchant persistMerchant(String status, String emailSuffix) {
        String email = "m-" + UUID.randomUUID() + emailSuffix;
        Merchant merchant = new Merchant(UUID.randomUUID(), "Acme " + emailSuffix, email, status);
        return merchantRepository.save(merchant);
    }

    // ── CU-02 / HU 1.2 ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "MERCHANT")
    void updateContact_returns200_whenPayloadValid() throws Exception {
        Merchant m = persistMerchant("ACTIVE", "@contact.ok");

        mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contact_name": "María López",
                                  "contact_email": "maria.lopez@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(m.getId().toString()))
                .andExpect(jsonPath("$.message").value("Contact information updated successfully"));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void updateContact_returns400_whenEmailInvalid() throws Exception {
        Merchant m = persistMerchant("ACTIVE", "@contact.bad");

        mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contact_name": "Nombre",
                                  "contact_email": "no-es-un-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void updateContact_returns404_whenMerchantMissing() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/merchants/{id}/contact", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contact_name": "Nombre",
                                  "contact_email": "ok@example.com"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("MERCHANT_NOT_FOUND"));
    }

    // ── CU-03 / HU 1.3 ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerBankAccount_returns200_whenMerchantActiveAndDataValid() throws Exception {
        Merchant m = persistMerchant("ACTIVE", "@bank.ok");

        mockMvc.perform(put("/api/v1/merchants/{id}/bank-account", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "iban": "ES9121000418450200051332",
                                  "routing_number": "021000089",
                                  "account_holder_name": "Acme S.L."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Bank account registered successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerBankAccount_returns403_whenMerchantInactive() throws Exception {
        Merchant m = persistMerchant("INACTIVE", "@bank.inactive");

        mockMvc.perform(put("/api/v1/merchants/{id}/bank-account", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "iban": "ES9121000418450200051332",
                                  "routing_number": "021000089",
                                  "account_holder_name": "Acme S.L."
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("MERCHANT_INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerBankAccount_returns400_whenIbanInvalid() throws Exception {
        Merchant m = persistMerchant("ACTIVE", "@bank.invalid");

        mockMvc.perform(put("/api/v1/merchants/{id}/bank-account", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "iban": "ES00INVALID00000000000000",
                                  "routing_number": "021000089",
                                  "account_holder_name": "Acme S.L."
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    // ── CU-04 / HU 1.4 ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void activate_returns200_whenMerchantInactive() throws Exception {
        Merchant m = persistMerchant("INACTIVE", "@activate");

        mockMvc.perform(patch("/api/v1/merchants/{id}/activate", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "reason": "Documentación aprobada" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("Merchant activated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activate_returns404_whenMerchantMissing() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/merchants/{id}/activate", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"reason\": \"test\" }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("MERCHANT_NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activate_returns422_whenTransitionInvalid() throws Exception {
        Merchant m = persistMerchant("ACTIVE", "@already-active");

        mockMvc.perform(patch("/api/v1/merchants/{id}/activate", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"reason\": \"Reactivar\" }"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATE_TRANSITION"));
    }

    // ── CU-05 / HU 1.5 ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivate_returns200_whenMerchantActive() throws Exception {
        Merchant m = persistMerchant("ACTIVE", "@deactivate");

        mockMvc.perform(patch("/api/v1/merchants/{id}/deactivate", m.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "reason": "Solicitud del comercio" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.message").value("Merchant deactivated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivate_returns404_whenMerchantMissing() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/merchants/{id}/deactivate", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"reason\": \"test\" }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("MERCHANT_NOT_FOUND"));
    }

    // ── CU-06 / HU 1.6 ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMerchant_returns200_whenExists() throws Exception {
        Merchant m = persistMerchant("INACTIVE", "@detail");

        mockMvc.perform(get("/api/v1/merchants/{id}", m.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(m.getId().toString()))
                .andExpect(jsonPath("$.name").value(m.getName()))
                .andExpect(jsonPath("$.email").value(m.getEmail()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMerchant_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/merchants/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("MERCHANT_NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void getMerchant_returns200_whenMerchantRole() throws Exception {
        Merchant m = persistMerchant("ACTIVE", "@detail.merchant");

        mockMvc.perform(get("/api/v1/merchants/{id}", m.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(m.getId().toString()));
    }

    @Test
    void getMerchant_returnsForbidden_whenAnonymous() throws Exception {
        Merchant m = persistMerchant("ACTIVE", "@anon");

        mockMvc.perform(get("/api/v1/merchants/{id}", m.getId()))
                .andExpect(status().isForbidden());
    }
}
