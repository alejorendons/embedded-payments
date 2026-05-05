package com.paymentplatform.embeddedpayments.merchant.domain.exception;

import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.util.List;
import org.springframework.http.HttpStatus;

public class MerchantInactiveException extends DomainException {

    public MerchantInactiveException(String message) {
        super(HttpStatus.FORBIDDEN, "MERCHANT_INACTIVE", message, List.of());
    }
}

