package net.tanguydev.settlementservice.Domain.Events;

import net.tanguydev.settlementservice.Domain.Enums.BatchEventType;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.NetPositionStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BatchEventEntry {

    private UUID id;
    private UUID batchId;
    private BatchEventType eventType;
    private Long aggregateVersion;
    private OffsetDateTime occurredAt;

    private String reference;
    private SettlementCycle cycle;
    private String currency;
    private BatchStatus status;

    private UUID paymentId;
    private String paymentReference;
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private BigDecimal amount;

    private UUID walletId;
    private BigDecimal grossDebit;
    private BigDecimal grossCredit;
    private BigDecimal netAmount;
    private NetPositionStatus positionStatus;

    private String reason;

    public BatchEventEntry() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public BatchEventType getEventType() {
        return eventType;
    }

    public void setEventType(BatchEventType eventType) {
        this.eventType = eventType;
    }

    public Long getAggregateVersion() {
        return aggregateVersion;
    }

    public void setAggregateVersion(Long aggregateVersion) {
        this.aggregateVersion = aggregateVersion;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public SettlementCycle getCycle() {
        return cycle;
    }

    public void setCycle(SettlementCycle cycle) {
        this.cycle = cycle;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public UUID getSenderWalletId() {
        return senderWalletId;
    }

    public void setSenderWalletId(UUID senderWalletId) {
        this.senderWalletId = senderWalletId;
    }

    public UUID getReceiverWalletId() {
        return receiverWalletId;
    }

    public void setReceiverWalletId(UUID receiverWalletId) {
        this.receiverWalletId = receiverWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getGrossDebit() {
        return grossDebit;
    }

    public void setGrossDebit(BigDecimal grossDebit) {
        this.grossDebit = grossDebit;
    }

    public BigDecimal getGrossCredit() {
        return grossCredit;
    }

    public void setGrossCredit(BigDecimal grossCredit) {
        this.grossCredit = grossCredit;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public NetPositionStatus getPositionStatus() {
        return positionStatus;
    }

    public void setPositionStatus(NetPositionStatus positionStatus) {
        this.positionStatus = positionStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
