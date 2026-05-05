package com.paymentplatform.embeddedpayments.merchant.domain.exception;

import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import java.util.List;
import org.springframework.http.HttpStatus;

public class MerchantNotFoundException extends DomainException {

    public MerchantNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "MERCHANT_NOT_FOUND", message, List.of());
    }
}

