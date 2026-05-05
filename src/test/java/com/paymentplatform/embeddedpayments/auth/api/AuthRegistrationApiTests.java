package com.paymentplatform.embeddedpayments.auth.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthRegistrationApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterAdminUserAndAuthenticate() throws Exception {
        String registerPayload = """
                {
                  "email":"admin+flow@test.com",
                  "password":"Passw0rd!",
                  "role":"ADMIN",
                  "merchantName":"Admin Commerce"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin+flow@test.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.merchantId").isNotEmpty())
                .andExpect(jsonPath("$.apiKey").isNotEmpty());

        String loginPayload = """
                {
                  "email":"admin+flow@test.com",
                  "password":"Passw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = extractToken(loginResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin+flow@test.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.merchantId").isNotEmpty());
    }

    @Test
    void shouldRegisterNaturalUserWithoutMerchant() throws Exception {
        String registerPayload = """
                {
                  "email":"buyer+flow@test.com",
                  "password":"Passw0rd!",
                  "role":"USER"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("buyer+flow@test.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.merchantId").isEmpty())
                .andExpect(jsonPath("$.apiKey").isEmpty());
    }

    private String extractToken(String responseBody) {
        String prefix = "\"token\":\"";
        int start = responseBody.indexOf(prefix);
        assertThat(start).isGreaterThanOrEqualTo(0);
        int from = start + prefix.length();
        int end = responseBody.indexOf('"', from);
        assertThat(end).isGreaterThan(from);
        return responseBody.substring(from, end);
    }
}
