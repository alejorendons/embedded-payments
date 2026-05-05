package com.paymentplatform.embeddedpayments.shared.security;

import com.paymentplatform.embeddedpayments.auth.domain.entity.MerchantApiKey;
import com.paymentplatform.embeddedpayments.auth.domain.repository.MerchantApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final MerchantApiKeyRepository merchantApiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyAuthenticationFilter(MerchantApiKeyRepository merchantApiKeyRepository,
                                      PasswordEncoder passwordEncoder) {
        this.merchantApiKeyRepository = merchantApiKeyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/api/v1/payments")
                || path.startsWith("/api/v1/transactions")
                || path.startsWith("/api/v1/refunds"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawApiKey = request.getHeader(API_KEY_HEADER);
        if (rawApiKey == null || rawApiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        MerchantApiKey apiKey = findByRawApiKey(rawApiKey);
        if (apiKey != null && apiKey.isActive() && passwordEncoder.matches(rawApiKey, apiKey.getKeyHash())) {
            String merchantId = apiKey.getMerchantId().toString();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    merchantId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
            );
            authentication.setDetails(Map.of("merchantId", merchantId));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private MerchantApiKey findByRawApiKey(String rawApiKey) {
        String[] parts = rawApiKey.split("_", 3);
        if (parts.length != 3 || !"epk".equals(parts[0])) {
            return null;
        }

        try {
            UUID keyId = UUID.fromString(parts[1]);
            return merchantApiKeyRepository.findById(keyId).orElse(null);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
