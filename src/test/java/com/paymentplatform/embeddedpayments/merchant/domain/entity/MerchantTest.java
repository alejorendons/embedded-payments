package com.paymentplatform.embeddedpayments.merchant.domain.entity;

import com.paymentplatform.embeddedpayments.merchant.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerchantTest {

    private Merchant buildMerchant(String status) {
        Merchant m = new Merchant(UUID.randomUUID(), "Test Merchant", "test@merchant.com", status);
        return m;
    }

    // ── changeStatus / transiciones válidas ──────────────────────────────────

    @Test
    void debeTransicionarDeInactiveAActive() {
        Merchant merchant = buildMerchant("INACTIVE");
        merchant.changeStatus("ACTIVE");
        assertThat(merchant.getStatus()).isEqualTo("ACTIVE");
        assertThat(merchant.getPreviousStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void debeTransicionarDeInactiveADisabled() {
        Merchant merchant = buildMerchant("INACTIVE");
        merchant.changeStatus("DISABLED");
        assertThat(merchant.getStatus()).isEqualTo("DISABLED");
    }

    @Test
    void debeTransicionarDeActiveAInactive() {
        Merchant merchant = buildMerchant("ACTIVE");
        merchant.changeStatus("INACTIVE");
        assertThat(merchant.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void debeTransicionarDeActiveASuspended() {
        Merchant merchant = buildMerchant("ACTIVE");
        merchant.changeStatus("SUSPENDED");
        assertThat(merchant.getStatus()).isEqualTo("SUSPENDED");
    }

    @Test
    void debeTransicionarDeSuspendedAActive() {
        Merchant merchant = buildMerchant("SUSPENDED");
        merchant.changeStatus("ACTIVE");
        assertThat(merchant.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void debeTransicionarDeSuspendedAInactive() {
        Merchant merchant = buildMerchant("SUSPENDED");
        merchant.changeStatus("INACTIVE");
        assertThat(merchant.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void debeTransicionarDeDisabledAInactive() {
        Merchant merchant = buildMerchant("DISABLED");
        merchant.changeStatus("INACTIVE");
        assertThat(merchant.getStatus()).isEqualTo("INACTIVE");
    }

    // ── changeStatus / transiciones inválidas ────────────────────────────────

    @Test
    void debeLanzarExcepcionEnTransicionInvalidaInactiveASuspended() {
        Merchant merchant = buildMerchant("INACTIVE");
        assertThatThrownBy(() -> merchant.changeStatus("SUSPENDED"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void debeLanzarExcepcionEnTransicionInvalidaActiveADisabledSiYaEsDisabled() {
        Merchant merchant = buildMerchant("DISABLED");
        // DISABLED -> SUSPENDED es inválida
        assertThatThrownBy(() -> merchant.changeStatus("SUSPENDED"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    // ── updateContact ─────────────────────────────────────────────────────────

    @Test
    void debeActualizarContactoCuandoDatosSonValidos() {
        Merchant merchant = buildMerchant("ACTIVE");
        merchant.updateContact("Juan Pérez", "juan@ejemplo.com");
        assertThat(merchant.getContactName()).isEqualTo("Juan Pérez");
        assertThat(merchant.getContactEmail()).isEqualTo("juan@ejemplo.com");
    }

    @Test
    void noDebeActualizarContactoCuandoDatosSonNulosOBlancos() {
        Merchant merchant = buildMerchant("ACTIVE");
        merchant.updateContact("Juan", "juan@ejemplo.com");
        merchant.updateContact(null, "  ");  // nulo y blank → no deben pisar los anteriores
        assertThat(merchant.getContactName()).isEqualTo("Juan");
        assertThat(merchant.getContactEmail()).isEqualTo("juan@ejemplo.com");
    }

    // ── registerBankAccount ───────────────────────────────────────────────────

    @Test
    void debeRegistrarCuentaBancariaCuandoDatosSonValidos() {
        Merchant merchant = buildMerchant("ACTIVE");
        byte[] encData = new byte[]{1, 2, 3};
        merchant.registerBankAccount("enc-data", "hash-value", encData);
        assertThat(merchant.getBankAccountEncrypted()).isEqualTo("enc-data");
        assertThat(merchant.getBankAccountHash()).isEqualTo("hash-value");
    }

    @Test
    void debeLanzarExcepcionCuandoBankAccountEncryptedEsBlank() {
        Merchant merchant = buildMerchant("ACTIVE");
        assertThatThrownBy(() -> merchant.registerBankAccount("", "hash", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bank account encrypted data cannot be empty");
    }

    @Test
    void debeLanzarExcepcionCuandoBankAccountHashEsBlank() {
        Merchant merchant = buildMerchant("ACTIVE");
        assertThatThrownBy(() -> merchant.registerBankAccount("enc", null, new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bank account hash cannot be empty");
    }

    // ── isActive / isInactive ─────────────────────────────────────────────────

    @Test
    void debeRetornarIsActiveCorrectamente() {
        assertThat(buildMerchant("ACTIVE").isActive()).isTrue();
        assertThat(buildMerchant("INACTIVE").isActive()).isFalse();
    }

    @Test
    void debeRetornarIsInactiveCorrectamente() {
        assertThat(buildMerchant("INACTIVE").isInactive()).isTrue();
        assertThat(buildMerchant("ACTIVE").isInactive()).isFalse();
    }
}
