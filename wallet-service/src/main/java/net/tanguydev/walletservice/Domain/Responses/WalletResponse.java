package net.tanguydev.walletservice.Domain.Responses;

import net.tanguydev.walletservice.Domain.Enums.WalletStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class WalletResponse {

    private Long id;
    private Long customerId;
    private String currency;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private BigDecimal availableBalance;
    private WalletStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public WalletResponse() {
    }

    public WalletResponse(Long id, Long customerId, String currency, BigDecimal balance,
                          BigDecimal frozenAmount, BigDecimal availableBalance, WalletStatus status,
                          OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.currency = currency;
        this.balance = balance;
        this.frozenAmount = frozenAmount;
        this.availableBalance = availableBalance;
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

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
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
}
