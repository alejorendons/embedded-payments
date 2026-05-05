package com.paymentplatform.embeddedpayments.transaction.domain.services;

import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.transaction.domain.entity.PaymentTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionDomainServiceTest {

    private TransactionDomainService transactionDomainService;

    @BeforeEach
    void setUp() {
        transactionDomainService = new TransactionDomainService();
    }

    @Test
    void debeRegistrarTransaccionCuandoDatosSonValidos() {
        // Arrange
        UUID paymentIntentId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200.00");

        // Act
        PaymentTransaction result = transactionDomainService.register(paymentIntentId, amount);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getPaymentIntentId()).isEqualTo(paymentIntentId);
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.getStatus()).isEqualTo("SUCCEEDED");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void debeLanzarExcepcionCuandoPaymentIntentIdEsNulo() {
        // Act & Assert
        assertThatThrownBy(() -> transactionDomainService.register(null, new BigDecimal("100.00")))
                .isInstanceOf(DomainException.class)
                .hasMessage("paymentIntentId is required");
    }

    @Test
    void debeLanzarExcepcionCuandoAmountEsCeroONegativo() {
        // Arrange
        UUID paymentIntentId = UUID.randomUUID();

        // Act & Assert
        assertThatThrownBy(() -> transactionDomainService.register(paymentIntentId, BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .hasMessage("amount must be greater than zero");

        assertThatThrownBy(() -> transactionDomainService.register(paymentIntentId, new BigDecimal("-1.00")))
                .isInstanceOf(DomainException.class)
                .hasMessage("amount must be greater than zero");

        assertThatThrownBy(() -> transactionDomainService.register(paymentIntentId, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("amount must be greater than zero");
    }
}
