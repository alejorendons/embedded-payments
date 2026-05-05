package com.paymentplatform.embeddedpayments.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class IBANValidator implements ConstraintValidator<ValidIBAN, String> {

    private static final Pattern IBAN_PATTERN = Pattern.compile(
        "^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        // Validar formato básico
        if (!IBAN_PATTERN.matcher(value).matches()) {
            return false;
        }

        // Validar checksum ISO 7064
        return validateIBANChecksum(value);
    }

    private boolean validateIBANChecksum(String iban) {
        // Rearranging: mover los primeros 4 caracteres al final
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Reemplazar letras con números (A=10, B=11, ..., Z=35)
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append(c - 'A' + 10);
            } else {
                numeric.append(c);
            }
        }

        // Validar checksum mod 97
        long number = 0;
        for (char c : numeric.toString().toCharArray()) {
            number = (number * 10 + (c - '0')) % 97;
        }

        return number == 1;
    }
}

