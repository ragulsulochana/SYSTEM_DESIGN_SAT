package com.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @NotBlank(message = "Member name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Email address is required")
    @Email(message = "Must be a valid email address")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "total_checked_out", nullable = false)
    private int totalCheckedOut = 0;

    @Column(name = "country")
    private String country = "United States";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Member() {}

    public Member(String id, String name, String email, AccountStatus accountStatus, int totalCheckedOut, String country) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.accountStatus = accountStatus;
        this.totalCheckedOut = totalCheckedOut;
        this.country = country;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public int getTotalCheckedOut() {
        return totalCheckedOut;
    }

    public void setTotalCheckedOut(int totalCheckedOut) {
        this.totalCheckedOut = totalCheckedOut;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
