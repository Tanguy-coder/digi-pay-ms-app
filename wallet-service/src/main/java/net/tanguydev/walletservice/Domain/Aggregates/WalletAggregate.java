package net.tanguydev.walletservice.Domain.Aggregates;

import net.tanguydev.walletservice.Domain.Enums.WalletEventType;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Validations.Exception.InsufficientBalanceException;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotActiveException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WalletAggregate {

    private UUID walletId;
    private UUID customerId;
    private String walletNumber;
    private WalletType walletType;
    private String currency;
    private BigDecimal balance = BigDecimal.ZERO;
    private BigDecimal frozenAmount = BigDecimal.ZERO;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private WalletStatus status;
    private long version = 0;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private final List<WalletEventEntry> uncommittedEvents = new ArrayList<>();

    public WalletAggregate() {}

    public static WalletAggregate reconstitute(List<WalletEventEntry> events) {
        WalletAggregate aggregate = new WalletAggregate();
        for (WalletEventEntry event : events) {
            aggregate.apply(event);
            aggregate.version = event.getAggregateVersion();
        }
        return aggregate;
    }

    public void createWallet(UUID walletId, UUID customerId, String walletNumber,
                             WalletType walletType, String currency,
                             BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        WalletEventEntry event = new WalletEventEntry();
        event.setWalletId(walletId);
        event.setEventType(WalletEventType.WALLET_CREATED);
        event.setCustomerId(customerId);
        event.setWalletNumber(walletNumber);
        event.setWalletType(walletType);
        event.setCurrency(currency);
        event.setDailyLimit(dailyLimit);
        event.setMonthlyLimit(monthlyLimit);
        event.setStatus(WalletStatus.ACTIVE);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    public void credit(BigDecimal amount) {
        requireActive();

        WalletEventEntry event = new WalletEventEntry();
        event.setWalletId(walletId);
        event.setEventType(WalletEventType.WALLET_CREDITED);
        event.setAmount(amount);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    public void debit(BigDecimal amount) {
        requireActive();

        BigDecimal available = balance.subtract(frozenAmount);
        if (amount.compareTo(available) > 0) {
            throw new InsufficientBalanceException(amount, available);
        }

        WalletEventEntry event = new WalletEventEntry();
        event.setWalletId(walletId);
        event.setEventType(WalletEventType.WALLET_DEBITED);
        event.setAmount(amount);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    public void freezeAmount(BigDecimal amount) {
        requireActive();

        BigDecimal available = balance.subtract(frozenAmount);
        if (amount.compareTo(available) > 0) {
            throw new InsufficientBalanceException(amount, available);
        }

        WalletEventEntry event = new WalletEventEntry();
        event.setWalletId(walletId);
        event.setEventType(WalletEventType.AMOUNT_FROZEN);
        event.setAmount(amount);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    private void apply(WalletEventEntry event) {
        switch (event.getEventType()) {
            case WALLET_CREATED -> {
                this.walletId = event.getWalletId();
                this.customerId = event.getCustomerId();
                this.walletNumber = event.getWalletNumber();
                this.walletType = event.getWalletType();
                this.currency = event.getCurrency();
                this.dailyLimit = event.getDailyLimit();
                this.monthlyLimit = event.getMonthlyLimit();
                this.status = event.getStatus();
                this.balance = BigDecimal.ZERO;
                this.frozenAmount = BigDecimal.ZERO;
                this.createdAt = event.getOccurredAt();
                this.updatedAt = event.getOccurredAt();
            }
            case WALLET_CREDITED -> {
                this.balance = this.balance.add(event.getAmount());
                this.updatedAt = event.getOccurredAt();
            }
            case WALLET_DEBITED -> {
                this.balance = this.balance.subtract(event.getAmount());
                this.updatedAt = event.getOccurredAt();
            }
            case AMOUNT_FROZEN -> {
                this.frozenAmount = this.frozenAmount.add(event.getAmount());
                this.updatedAt = event.getOccurredAt();
            }
            case AMOUNT_UNFROZEN -> {
                this.frozenAmount = this.frozenAmount.subtract(event.getAmount());
                this.updatedAt = event.getOccurredAt();
            }
        }
        this.version = event.getAggregateVersion();
    }

    private void requireActive() {
        if (this.status != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(this.walletId);
        }
    }

    public List<WalletEventEntry> getUncommittedEvents() {
        return List.copyOf(uncommittedEvents);
    }

    public void markEventsCommitted() {
        uncommittedEvents.clear();
    }

    public UUID getWalletId() { return walletId; }
    public UUID getCustomerId() { return customerId; }
    public String getWalletNumber() { return walletNumber; }
    public WalletType getWalletType() { return walletType; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getFrozenAmount() { return frozenAmount; }
    public BigDecimal getAvailableBalance() { return balance.subtract(frozenAmount); }
    public BigDecimal getDailyLimit() { return dailyLimit; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public WalletStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
