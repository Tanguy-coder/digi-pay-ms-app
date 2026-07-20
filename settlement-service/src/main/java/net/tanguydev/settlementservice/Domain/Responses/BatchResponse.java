package net.tanguydev.settlementservice.Domain.Responses;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class BatchResponse {

    private UUID id;
    private String reference;
    private String status;
    private String cycle;
    private String currency;
    private int totalEntries;
    private BigDecimal totalAmount;
    private OffsetDateTime openedAt;
    private OffsetDateTime closedAt;
    private OffsetDateTime settledAt;
    private List<EntryResponse> entries;
    private List<NetPositionResponse> positions;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
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

    public List<EntryResponse> getEntries() {
        return entries;
    }

    public void setEntries(List<EntryResponse> entries) {
        this.entries = entries;
    }

    public List<NetPositionResponse> getPositions() {
        return positions;
    }

    public void setPositions(List<NetPositionResponse> positions) {
        this.positions = positions;
    }
}
