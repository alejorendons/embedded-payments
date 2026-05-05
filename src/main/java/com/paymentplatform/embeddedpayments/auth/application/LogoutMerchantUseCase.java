package com.paymentplatform.embeddedpayments.auth.application;

import org.springframework.stereotype.Service;

@Service
public class LogoutMerchantUseCase {

    public LogoutMerchantUseCase() {
    }

    public void execute() {
        // Logout is handled on the frontend side by removing the token
        // The backend just returns a success response
    }
}
