package com.paymentplatform.embeddedpayments.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey;
import com.paymentplatform.embeddedpayments.auth.domain.repository.MerchantApiKeyRepository;
import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

    @Mock
    private MerchantApiKeyRepository merchantApiKeyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FilterChain filterChain;

    private ApiKeyAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiKeyAuthenticationFilter(merchantApiKeyRepository, passwordEncoder);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilter_excluyeRutasMerchants() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/v1/merchants/x");

        assertThat(filter.shouldNotFilter(req)).isTrue();
    }

    @Test
    void shouldNotFilter_procesaPaymentsTransactionsRefunds() {
        assertThat(filter.shouldNotFilter(uri("/api/v1/payments/intents"))).isFalse();
        assertThat(filter.shouldNotFilter(uri("/api/v1/transactions"))).isFalse();
        assertThat(filter.shouldNotFilter(uri("/api/v1/refunds"))).isFalse();
    }

    private static MockHttpServletRequest uri(String path) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI(path);
        return req;
    }

    @Test
    void delegaSinConsultarRepositorioCuandoYaHayAutenticacion() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u", null,
                        java.util.List.of(() -> "ROLE_USER")));

        MockHttpServletRequest req = uri("/api/v1/refunds");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
        verify(merchantApiKeyRepository, never()).findById(any());
    }

    @Test
    void continuaSinApiKey() throws Exception {
        MockHttpServletRequest req = uri("/api/v1/refunds");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void formatoInvalidoNoConsultaRepositorio() throws Exception {
        MockHttpServletRequest req = uri("/api/v1/refunds");
        req.addHeader("X-API-Key", "no-formato-valido");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(merchantApiKeyRepository, never()).findById(any());
    }

    @Test
    void uuidInvalidoEnClave_noAutentica() throws Exception {
        MockHttpServletRequest req = uri("/api/v1/transactions");
        req.addHeader("X-API-Key", "epk_no-uuid_suffix");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(merchantApiKeyRepository, never()).findById(any());
    }

    @Test
    void autenticaCuandoClaveCoincideYEstaActiva() throws Exception {
        UUID keyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        String raw = "epk_" + keyId + "_secretPart";
        MerchantApiKey apiKey = new MerchantApiKey(keyId, merchantId, "stored-hash", "ACTIVE", Instant.now());

        MockHttpServletRequest req = uri("/api/v1/transactions");
        req.addHeader("X-API-Key", raw);
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(merchantApiKeyRepository.findById(keyId)).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(raw, "stored-hash")).thenReturn(true);

        filter.doFilterInternal(req, res, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(merchantId.toString());
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_API_CLIENT");
    }

    @Test
    void claveInactivaNoAutentica() throws Exception {
        UUID keyId = UUID.randomUUID();
        String raw = "epk_" + keyId + "_x";
        MerchantApiKey apiKey = new MerchantApiKey(keyId, UUID.randomUUID(), "h", "REVOKED", Instant.now());

        MockHttpServletRequest req = uri("/api/v1/refunds");
        req.addHeader("X-API-Key", raw);
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(merchantApiKeyRepository.findById(keyId)).thenReturn(Optional.of(apiKey));

        filter.doFilterInternal(req, res, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void hashNoCoincideNoAutentica() throws Exception {
        UUID keyId = UUID.randomUUID();
        String raw = "epk_" + keyId + "_x";
        MerchantApiKey apiKey = new MerchantApiKey(keyId, UUID.randomUUID(), "h", "ACTIVE", Instant.now());

        MockHttpServletRequest req = uri("/api/v1/refunds");
        req.addHeader("X-API-Key", raw);
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(merchantApiKeyRepository.findById(keyId)).thenReturn(Optional.of(apiKey));
        when(passwordEncoder.matches(raw, "h")).thenReturn(false);

        filter.doFilterInternal(req, res, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
