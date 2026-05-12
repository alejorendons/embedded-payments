package com.paymentplatform.embeddedpayments.refund.application;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import com.paymentplatform.embeddedpayments.refund.domain.repository.RefundRepository;
import com.paymentplatform.embeddedpayments.refund.domain.services.RefundDomainService;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import com.paymentplatform.embeddedpayments.transaction.domain.repository.TransactionRepository;
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
class CreateRefundUseCaseTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundDomainService refundDomainService;

    @InjectMocks
    private CreateRefundUseCase useCase;

    @Test
    void debeCrearRefundCuandoDatosSonValidos() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");
        String reason = "Producto defectuoso";

        PaymentTransaction mockTx = new PaymentTransaction(transactionId, paymentIntentId, amount, "SUCCEEDED", Instant.now());
        PaymentIntent mockIntent = new PaymentIntent(paymentIntentId, merchantId, amount, "USD", "CREATED");
        Refund mockRefund = new Refund(UUID.randomUUID(), transactionId, amount, reason, "PENDING");

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(mockTx));
        when(paymentRepository.findById(paymentIntentId)).thenReturn(Optional.of(mockIntent));
        when(refundDomainService.create(transactionId, amount, reason)).thenReturn(mockRefund);
        when(refundRepository.save(any())).thenReturn(mockRefund);

        // Act
        Refund result = useCase.execute(merchantId, transactionId, amount, reason);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(refundRepository, times(1)).save(mockRefund);
    }

    @Test
    void debeLanzarNotFoundCuandoTransaccionNoExiste() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), transactionId, new BigDecimal("50.00"), "Motivo"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Transaction not found");

        verify(refundRepository, never()).save(any());
    }

    @Test
    void debeLanzarForbiddenCuandoMerchantNoEsDueno() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        UUID otroMerchantId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");

        PaymentTransaction mockTx = new PaymentTransaction(transactionId, paymentIntentId, amount, "SUCCEEDED", Instant.now());
        PaymentIntent intentDeOtroMerchant = new PaymentIntent(paymentIntentId, otroMerchantId, amount, "USD", "CREATED");

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(mockTx));
        when(paymentRepository.findById(paymentIntentId)).thenReturn(Optional.of(intentDeOtroMerchant));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(merchantId, transactionId, amount, "Motivo"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Transaction does not belong to authenticated merchant");

        verify(refundRepository, never()).save(any());
    }

    @Test
    void debeLanzarNotFoundCuandoPaymentIntentNoExiste() {
        UUID merchantId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID paymentIntentId = UUID.randomUUID();

        PaymentTransaction mockTx = new PaymentTransaction(transactionId, paymentIntentId, new BigDecimal("10"), "SUCCEEDED", Instant.now());

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(mockTx));
        when(paymentRepository.findById(paymentIntentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(merchantId, transactionId, new BigDecimal("10"), "x"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Payment intent not found");

        verify(refundRepository, never()).save(any());
    }
}
