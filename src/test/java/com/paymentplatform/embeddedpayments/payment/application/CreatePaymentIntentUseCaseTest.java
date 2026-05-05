package com.paymentplatform.embeddedpayments.payment.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.payment.domain.services.PaymentDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePaymentIntentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDomainService paymentDomainService;

    @InjectMocks
    private CreatePaymentIntentUseCase useCase;

    @Test
    void debeOrquestarCreacionYGuardadoDelPaymentIntent() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.00");
        String currency = "EUR";

        PaymentIntent mockIntent = new PaymentIntent(UUID.randomUUID(), merchantId, amount, currency, "CREATED");

        when(paymentDomainService.createPaymentIntent(merchantId, amount, currency)).thenReturn(mockIntent);
        when(paymentRepository.save(any(PaymentIntent.class))).thenReturn(mockIntent);

        // Act
        PaymentIntent result = useCase.execute(merchantId, amount, currency);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMerchantId()).isEqualTo(merchantId);
        
        verify(paymentDomainService, times(1)).createPaymentIntent(merchantId, amount, currency);
        verify(paymentRepository, times(1)).save(mockIntent);
    }
}
