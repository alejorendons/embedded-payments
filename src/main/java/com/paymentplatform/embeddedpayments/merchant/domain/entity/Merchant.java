package com.paymentplatform.embeddedpayments.merchant.domain.entity;

import com.paymentplatform.embeddedpayments.merchant.domain.exception.InvalidStateTransitionException;
import com.paymentplatform.embeddedpayments.shared.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant extends AuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(nullable = false)
    private String status;

    @Column(name = "bank_account_encrypted")
    private String bankAccountEncrypted;

    @Column(name = "bank_account_hash")
    private String bankAccountHash;

    @Column(name = "encrypted_bank_data")
    private byte[] encryptedBankData;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Merchant() {
    }

    public Merchant(UUID id, String name, String email, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getStatus() {
        return status;
    }

    public String getBankAccountEncrypted() {
        return bankAccountEncrypted;
    }

    public String getBankAccountHash() {
        return bankAccountHash;
    }

    public byte[] getEncryptedBankData() {
        return encryptedBankData;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters con validación
    public void updateContact(String contactName, String contactEmail) {
        if (contactName != null && !contactName.isBlank()) {
            this.contactName = contactName;
        }
        if (contactEmail != null && !contactEmail.isBlank()) {
            this.contactEmail = contactEmail;
        }
        this.updatedAt = Instant.now();
    }

    public void registerBankAccount(String bankAccountEncrypted, String bankAccountHash, byte[] encryptedBankData) {
        if (bankAccountEncrypted == null || bankAccountEncrypted.isBlank()) {
            throw new IllegalArgumentException("Bank account encrypted data cannot be empty");
        }
        if (bankAccountHash == null || bankAccountHash.isBlank()) {
            throw new IllegalArgumentException("Bank account hash cannot be empty");
        }
        
        this.bankAccountEncrypted = bankAccountEncrypted;
        this.bankAccountHash = bankAccountHash;
        this.encryptedBankData = encryptedBankData;
        this.updatedAt = Instant.now();
    }

    public void changeStatus(String newStatus) {
        validateStatusTransition(this.status, newStatus);
        this.previousStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        MerchantStatus current = MerchantStatus.valueOf(currentStatus);
        MerchantStatus target = MerchantStatus.valueOf(newStatus);
        
        if (!current.isValidTransition(target)) {
            throw new InvalidStateTransitionException(
                String.format("Cannot transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    public boolean isInactive() {
        return "INACTIVE".equals(this.status);
    }

    public enum MerchantStatus {
        INACTIVE {
            @Override
            public boolean isValidTransition(MerchantStatus target) {
                return target == ACTIVE || target == DISABLED;
            }
        },
        ACTIVE {
            @Override
            public boolean isValidTransition(MerchantStatus target) {
                return target == INACTIVE || target == SUSPENDED || target == DISABLED;
            }
        },
        SUSPENDED {
            @Override
            public boolean isValidTransition(MerchantStatus target) {
                return target == ACTIVE || target == INACTIVE || target == DISABLED;
            }
        },
        DISABLED {
            @Override
            public boolean isValidTransition(MerchantStatus target) {
                return target == INACTIVE || target == ACTIVE;
            }
        };

        public abstract boolean isValidTransition(MerchantStatus target);
    }
}

