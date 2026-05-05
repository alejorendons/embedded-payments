package com.paymentplatform.embeddedpayments.payment.domain.services;

import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentDomainServiceTest {

    private PaymentDomainService paymentDomainService;

    @BeforeEach
    void setUp() {
        paymentDomainService = new PaymentDomainService();
    }

    @Test
    void debeCrearPaymentIntentCuandoDatosSonValidos() {
        UUID merchantId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.50");
        String currency = "USD";

        PaymentIntent result = paymentDomainService.createPaymentIntent(merchantId, amount, currency);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getMerchantId()).isEqualTo(merchantId);
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getStatus()).isEqualTo("CREATED");
    }

    @Test
    void debeLanzarExcepcionCuandoMontoEsCeroOMenor() {
        UUID merchantId = UUID.randomUUID();
        String currency = "USD";

        assertThatThrownBy(() -> paymentDomainService.createPaymentIntent(merchantId, BigDecimal.ZERO, currency))
                .isInstanceOf(DomainException.class)
                .hasMessage("amount must be greater than zero");

        assertThatThrownBy(() -> paymentDomainService.createPaymentIntent(merchantId, new BigDecimal("-50.0"), currency))
                .isInstanceOf(DomainException.class)
                .hasMessage("amount must be greater than zero");

        assertThatThrownBy(() -> paymentDomainService.createPaymentIntent(merchantId, null, currency))
                .isInstanceOf(DomainException.class)
                .hasMessage("amount must be greater than zero");
    }

    @Test
    void debeLanzarExcepcionCuandoMonedaEsInvalida() {
        UUID merchantId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.50");

        assertThatThrownBy(() -> paymentDomainService.createPaymentIntent(merchantId, amount, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("currency must be a 3-letter ISO code");

        assertThatThrownBy(() -> paymentDomainService.createPaymentIntent(merchantId, amount, ""))
                .isInstanceOf(DomainException.class)
                .hasMessage("currency must be a 3-letter ISO code");

        assertThatThrownBy(() -> paymentDomainService.createPaymentIntent(merchantId, amount, "US"))
                .isInstanceOf(DomainException.class)
                .hasMessage("currency must be a 3-letter ISO code");

        assertThatThrownBy(() -> paymentDomainService.createPaymentIntent(merchantId, amount, "USDA"))
                .isInstanceOf(DomainException.class)
                .hasMessage("currency must be a 3-letter ISO code");
    }
}
