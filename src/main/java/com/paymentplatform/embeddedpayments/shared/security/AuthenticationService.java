package com.paymentplatform.embeddedpayments.shared.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    /**
     * Obtiene el usuario autenticado actual
     */
    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }

    /**
     * Obtiene el rol del usuario autenticado
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                return authority.getAuthority();
            }
        }
        return null;
    }

    /**
     * Verifica si el usuario es administrador
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        }
        return false;
    }

    /**
     * Verifica si el usuario es comercio
     */
    public boolean isMerchant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_MERCHANT".equals(auth.getAuthority()));
        }
        return false;
    }

    /**
     * Obtiene el ID del comercio del usuario autenticado (si es comercio)
     */
    public String getMerchantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> details = (java.util.Map<String, Object>) authentication.getDetails();
            Object merchantId = details.get("merchantId");
            if (merchantId != null) {
                return merchantId.toString();
            }
        }

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().anyMatch(auth -> "ROLE_MERCHANT".equals(auth.getAuthority()))) {
            return authentication.getName();
        }

        return null;
    }

    /**
     * Obtiene el UUID del comercio del usuario autenticado
     */
    public UUID getCurrentMerchantId() {
        String merchantId = getMerchantId();
        if (merchantId != null) {
            return UUID.fromString(merchantId);
        }
        return null;
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String principal = authentication.getName();
        if (principal == null || principal.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
