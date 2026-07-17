package net.tanguydev.walletservice.Domain.Events;

import net.tanguydev.walletservice.Domain.Enums.WalletStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class WalletEvent {

    private String eventType;
    private UUID walletId;
    private UUID customerId;
    private String currency;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private BigDecimal frozenAmountAfter;
    private WalletStatus status;
    private OffsetDateTime occurredAt;

    public WalletEvent() {}

    public WalletEvent(String eventType, UUID walletId, UUID customerId, String currency,
                       BigDecimal amount, BigDecimal balanceAfter, BigDecimal frozenAmountAfter,
                       WalletStatus status, OffsetDateTime occurredAt) {
        this.eventType = eventType;
        this.walletId = walletId;
        this.customerId = customerId;
        this.currency = currency;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.frozenAmountAfter = frozenAmountAfter;
        this.status = status;
        this.occurredAt = occurredAt;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public BigDecimal getFrozenAmountAfter() { return frozenAmountAfter; }
    public void setFrozenAmountAfter(BigDecimal frozenAmountAfter) { this.frozenAmountAfter = frozenAmountAfter; }

    public WalletStatus getStatus() { return status; }
    public void setStatus(WalletStatus status) { this.status = status; }

    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
}
