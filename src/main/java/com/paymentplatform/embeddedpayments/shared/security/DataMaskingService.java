package com.paymentplatform.embeddedpayments.shared.security;

import java.security.MessageDigest;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class DataMaskingService {

    /**
     * Enmascarar un email mostrando solo el dominio
     * Ejemplo: juan.mosquera@example.com -> ***@example.com
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        return "***@" + parts[1];
    }

    /**
     * Enmascarar IBAN mostrando solo los últimos 4 dígitos
     * Ejemplo: ES9121000418450200051332 -> ****...1332
     */
    public String maskIBAN(String iban) {
        if (iban == null || iban.length() < 4) {
            return "****";
        }
        return "****..." + iban.substring(iban.length() - 4);
    }

    /**
     * Enmascarar número de enrutamiento (routing number)
     * Solo muestra últimos 2 dígitos
     */
    public String maskRoutingNumber(String routingNumber) {
        if (routingNumber == null || routingNumber.length() < 2) {
            return "****";
        }
        return "****" + routingNumber.substring(routingNumber.length() - 2);
    }

    /**
     * Enmascarar nombre de cuenta bancaria
     * Primeros 2 caracteres + ****
     */
    public String maskAccountName(String accountName) {
        if (accountName == null || accountName.length() < 2) {
            return "****";
        }
        return accountName.substring(0, 2) + "****";
    }

    /**
     * Enmascarar teléfono (últimos 4 dígitos)
     * Ejemplo: +34-644-555-1234 -> +34-***-****-1234
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        String lastFour = phone.substring(phone.length() - 4);
        String prefix = phone.substring(0, Math.min(3, phone.length()));
        return prefix + "****" + lastFour;
    }

    /**
     * Generar hash seguro de un valor sensible (para búsquedas sin desencriptar)
     * Utiliza SHA-256
     */
    public String hashSensitiveValue(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    /**
     * Determina si un campo es sensible (según OWASP)
     */
    public boolean isSensitiveField(String fieldName) {
        return fieldName.toLowerCase().matches(
            ".*(?:email|phone|account|iban|routing|credit|debit|password|ssn|" +
            "password_hash|token|api_key|secret|cvv|card_number|bank).*"
        );
    }
}

