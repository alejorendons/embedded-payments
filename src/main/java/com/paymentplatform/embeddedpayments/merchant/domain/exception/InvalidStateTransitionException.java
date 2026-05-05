package com.paymentplatform.embeddedpayments.merchant.domain.exception;

import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.util.List;
import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_STATE_TRANSITION", message, List.of());
    }

    public InvalidStateTransitionException(String message, List<String> details) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_STATE_TRANSITION", message, details);
    }
}

