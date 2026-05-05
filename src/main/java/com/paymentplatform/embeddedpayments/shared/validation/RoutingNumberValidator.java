package com.paymentplatform.embeddedpayments.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class RoutingNumberValidator implements ConstraintValidator<ValidRoutingNumber, String> {

    private static final Pattern ROUTING_PATTERN = Pattern.compile("^\\d{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        // Validar formato: exactamente 9 dígitos
        if (!ROUTING_PATTERN.matcher(value).matches()) {
            return false;
        }

        // Validar checksum (algoritmo de routing numbers de EE.UU.)
        return validateRoutingChecksum(value);
    }

    private boolean validateRoutingChecksum(String routing) {
        int[] digits = new int[9];
        for (int i = 0; i < 9; i++) {
            digits[i] = Character.getNumericValue(routing.charAt(i));
        }

        // Multiplicadores para validación de routing
        int sum = (digits[0] + digits[1] * 7 + digits[2] * 3) +
                  (digits[3] + digits[4] * 7 + digits[5] * 3) +
                  (digits[6] + digits[7] * 7 + digits[8] * 3);

        return sum % 10 == 0;
    }
}

