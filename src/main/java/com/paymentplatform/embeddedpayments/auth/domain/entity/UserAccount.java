package com.paymentplatform.embeddedpayments.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<AuthRole> roles = new HashSet<>();

    protected UserAccount() {
    }

    public UserAccount(UUID id, String email, String passwordHash, String status, Instant createdAt, Set<AuthRole> roles) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.createdAt = createdAt;
        this.roles = roles;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<AuthRole> getRoles() {
        return roles;
    }

    public String getPrimaryRole() {
        if (roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()))) {
            return "ROLE_ADMIN";
        }
        if (roles.stream().anyMatch(role -> "ROLE_MERCHANT".equals(role.getName()))) {
            return "ROLE_MERCHANT";
        }
        return roles.stream()
                .map(AuthRole::getName)
                .findFirst()
                .orElse("ROLE_USER");
    }
}
