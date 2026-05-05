package com.paymentplatform.embeddedpayments.transaction.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
import com.paymentplatform.embeddedpayments.transaction.domain.services.TransactionDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TransactionDomainService transactionDomainService;

    @InjectMocks
    private CreateTransactionUseCase useCase;

    @Test
    void debeCrearTransaccionCuandoMerchantEsDuenoDelIntent() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("300.00");

        PaymentIntent mockIntent = new PaymentIntent(paymentIntentId, merchantId, amount, "USD", "CREATED");
        PaymentTransaction mockTx = new PaymentTransaction(UUID.randomUUID(), paymentIntentId, amount, "SUCCEEDED", Instant.now());

        when(paymentRepository.findById(paymentIntentId)).thenReturn(Optional.of(mockIntent));
        when(transactionDomainService.register(paymentIntentId, amount)).thenReturn(mockTx);
        when(transactionRepository.save(any())).thenReturn(mockTx);

        // Act
        PaymentTransaction result = useCase.execute(merchantId, paymentIntentId, amount);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("SUCCEEDED");
        verify(transactionRepository, times(1)).save(mockTx);
    }

    @Test
    void debeLanzarNotFoundCuandoPaymentIntentNoExiste() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();

        when(paymentRepository.findById(paymentIntentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(merchantId, paymentIntentId, new BigDecimal("100.00")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Payment intent not found");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void debeLanzarForbiddenCuandoMerchantNoEsDueno() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        UUID otroMerchantId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");

        PaymentIntent intentDeOtroMerchant = new PaymentIntent(paymentIntentId, otroMerchantId, amount, "USD", "CREATED");
        when(paymentRepository.findById(paymentIntentId)).thenReturn(Optional.of(intentDeOtroMerchant));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(merchantId, paymentIntentId, amount))
                .isInstanceOf(DomainException.class)
                .hasMessage("Payment intent does not belong to authenticated merchant");

        verify(transactionRepository, never()).save(any());
    }
}
