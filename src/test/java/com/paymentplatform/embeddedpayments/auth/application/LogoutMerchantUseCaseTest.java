package com.paymentplatform.embeddedpayments.auth.application;

import org.junit.jupiter.api.Test;

class LogoutMerchantUseCaseTest {

    @Test
    void execute_noLanza() {
        LogoutMerchantUseCase useCase = new LogoutMerchantUseCase();
        useCase.execute();
    }
}
