package net.tanguydev.walletservice.Domain.Entities;

import net.tanguydev.walletservice.Domain.Enums.WalletStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DomainWallet {

    private Long id;
    private Long customerId;
    private String currency;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private WalletStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public DomainWallet() {
    }

    public DomainWallet(Long id, Long customerId, String currency, BigDecimal balance,
                        BigDecimal frozenAmount, WalletStatus status,
                        OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.currency = currency;
        this.balance = balance;
        this.frozenAmount = frozenAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public void setStatus(WalletStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigDecimal getAvailableBalance() {
        return balance.subtract(frozenAmount);
    }
}
