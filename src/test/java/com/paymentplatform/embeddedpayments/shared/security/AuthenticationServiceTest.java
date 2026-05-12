package com.paymentplatform.embeddedpayments.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthenticationServiceTest {

    private final AuthenticationService service = new AuthenticationService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_devuelveAnonymousSinAutenticacion() {
        assertThat(service.getCurrentUser()).isEqualTo("anonymous");
    }

    @Test
    void getCurrentUser_devuelveNombreDelPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("principal@mail.com", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThat(service.getCurrentUser()).isEqualTo("principal@mail.com");
    }

    @Test
    void getCurrentUserRole_devuelvePrimeraAuthority() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        assertThat(service.getCurrentUserRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void getCurrentUserRole_nullSinAutenticacion() {
        assertThat(service.getCurrentUserRole()).isNull();
    }

    @Test
    void isAdmin_segúnAuthorities() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("a", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertThat(service.isAdmin()).isTrue();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("m", null,
                        List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))));
        assertThat(service.isAdmin()).isFalse();
    }

    @Test
    void isMerchant_segúnAuthorities() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("m", null,
                        List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))));
        assertThat(service.isMerchant()).isTrue();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("a", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertThat(service.isMerchant()).isFalse();
    }

    @Test
    void getMerchantId_priorizaDetailsMap() {
        UUID mid = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("ignored", null,
                List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT")));
        Map<String, Object> details = new HashMap<>();
        details.put("merchantId", mid.toString());
        auth.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(service.getMerchantId()).isEqualTo(mid.toString());
        assertThat(service.getCurrentMerchantId()).isEqualTo(mid);
    }

    @Test
    void getMerchantId_usaNombreCuandoRolMerchant() {
        UUID mid = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mid.toString(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))));

        assertThat(service.getMerchantId()).isEqualTo(mid.toString());
    }

    @Test
    void getCurrentUserId_parseaUuidDelPrincipal() {
        UUID id = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(id.toString(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThat(service.getCurrentUserId()).isEqualTo(id);
    }

    @Test
    void getCurrentUserId_nullCuandoPrincipalNoEsUuid() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("not-a-uuid", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThat(service.getCurrentUserId()).isNull();
    }

    @Test
    void getCurrentUserId_nullCuandoPrincipalVacio() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("  ", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThat(service.getCurrentUserId()).isNull();
    }
}
