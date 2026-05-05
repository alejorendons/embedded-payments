package com.paymentplatform.embeddedpayments.refund.domain.services;

import com.paymentplatform.embeddedpayments.refund.domain.entity.Refund;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundDomainServiceTest {

    private RefundDomainService refundDomainService;

    @BeforeEach
    void setUp() {
        refundDomainService = new RefundDomainService();
    }

    @Test
    void debeCrearRefundCuandoDatosSonValidos() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");
        String reason = "Producto defectuoso";

        // Act
        Refund result = refundDomainService.create(transactionId, amount, reason);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo(transactionId);
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.getReason()).isEqualTo(reason);
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void debeLanzarExcepcionCuandoTransactionIdEsNulo() {
        // Act & Assert
        assertThatThrownBy(() -> refundDomainService.create(null, new BigDecimal("50.00"), "Motivo válido"))
                .isInstanceOf(DomainException.class)
                .hasMessage("transactionId is required");
    }

    @Test
    void debeLanzarExcepcionCuandoAmountEsInvalido() {
        // Arrange
        UUID transactionId = UUID.randomUUID();

        // Act & Assert — cero
        assertThatThrownBy(() -> refundDomainService.create(transactionId, BigDecimal.ZERO, "Motivo"))
                .isInstanceOf(DomainException.class)
                .hasMessage("refund amount must be greater than zero");

        // negativo
        assertThatThrownBy(() -> refundDomainService.create(transactionId, new BigDecimal("-10.00"), "Motivo"))
                .isInstanceOf(DomainException.class)
                .hasMessage("refund amount must be greater than zero");

        // nulo
        assertThatThrownBy(() -> refundDomainService.create(transactionId, null, "Motivo"))
                .isInstanceOf(DomainException.class)
                .hasMessage("refund amount must be greater than zero");
    }

    @Test
    void debeLanzarExcepcionCuandoReasonEsInvalido() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");

        // Act & Assert — nulo
        assertThatThrownBy(() -> refundDomainService.create(transactionId, amount, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("refund reason is required");

        // blank
        assertThatThrownBy(() -> refundDomainService.create(transactionId, amount, "   "))
                .isInstanceOf(DomainException.class)
                .hasMessage("refund reason is required");
    }
}
