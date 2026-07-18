package net.tanguydev.settlementservice.Domain.Responses;

import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Enums.SettlementStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class SettlementResponse {
    private UUID id;
    private String reference;
    private SettlementStatus status;
    private SettlementCycle cycle;
    private String currency;
    private int totalPayments;
    private BigDecimal totalAmount;
    private BigDecimal netPosition;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
    private OffsetDateTime settledAt;
    private OffsetDateTime createdAt;
    private List<SettlementEntryResponse> entries;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }

    public SettlementCycle getCycle() { return cycle; }
    public void setCycle(SettlementCycle cycle) { this.cycle = cycle; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getTotalPayments() { return totalPayments; }
    public void setTotalPayments(int totalPayments) { this.totalPayments = totalPayments; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getNetPosition() { return netPosition; }
    public void setNetPosition(BigDecimal netPosition) { this.netPosition = netPosition; }

    public OffsetDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(OffsetDateTime periodStart) { this.periodStart = periodStart; }

    public OffsetDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(OffsetDateTime periodEnd) { this.periodEnd = periodEnd; }

    public OffsetDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(OffsetDateTime settledAt) { this.settledAt = settledAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public List<SettlementEntryResponse> getEntries() { return entries; }
    public void setEntries(List<SettlementEntryResponse> entries) { this.entries = entries; }
}
