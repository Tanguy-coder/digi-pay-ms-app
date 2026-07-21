package net.tanguydev.settlementservice.Domain.Entities;

import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DomainSettlementBatch {

    private UUID id;
    private String reference;
    private BatchStatus status;
    private SettlementCycle cycle;
    private String currency;
    private int totalEntries;
    private BigDecimal totalAmount;
    private OffsetDateTime openedAt;
    private OffsetDateTime closedAt;
    private OffsetDateTime settledAt;
    private OffsetDateTime createdAt;
    private List<DomainSettlementEntry> entries = new ArrayList<>();
    private List<DomainNetPosition> positions = new ArrayList<>();

    public DomainSettlementBatch() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
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

    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(OffsetDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public OffsetDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(OffsetDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DomainSettlementEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<DomainSettlementEntry> entries) {
        this.entries = entries;
    }

    public List<DomainNetPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<DomainNetPosition> positions) {
        this.positions = positions;
    }
}
