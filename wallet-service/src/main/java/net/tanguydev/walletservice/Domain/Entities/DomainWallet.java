package net.tanguydev.walletservice.Domain.Entities;

import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DomainWallet {

    private UUID id;
    private UUID customerId;
    private String walletNumber;
    private WalletType walletType;
    private String currency;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private WalletStatus status;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public DomainWallet() {}

    public DomainWallet(UUID id, UUID customerId, String walletNumber, WalletType walletType,
                        String currency, BigDecimal balance, BigDecimal frozenAmount,
                        BigDecimal dailyLimit, BigDecimal monthlyLimit,
                        WalletStatus status, Long version,
                        OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.walletNumber = walletNumber;
        this.walletType = walletType;
        this.currency = currency;
        this.balance = balance;
        this.frozenAmount = frozenAmount;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public BigDecimal getAvailableBalance() {
        return balance.subtract(frozenAmount);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getWalletNumber() { return walletNumber; }
    public void setWalletNumber(String walletNumber) { this.walletNumber = walletNumber; }

    public WalletType getWalletType() { return walletType; }
    public void setWalletType(WalletType walletType) { this.walletType = walletType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getFrozenAmount() { return frozenAmount; }
    public void setFrozenAmount(BigDecimal frozenAmount) { this.frozenAmount = frozenAmount; }

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }

    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public WalletStatus getStatus() { return status; }
    public void setStatus(WalletStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
