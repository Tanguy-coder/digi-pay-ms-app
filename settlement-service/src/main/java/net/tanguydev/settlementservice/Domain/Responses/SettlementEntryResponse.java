package net.tanguydev.settlementservice.Domain.Responses;

import net.tanguydev.settlementservice.Domain.Enums.EntryType;

import java.math.BigDecimal;
import java.util.UUID;

public class SettlementEntryResponse {
    private UUID id;
    private UUID paymentId;
    private String paymentReference;
    private UUID walletId;
    private EntryType entryType;
    private BigDecimal amount;
    private String currency;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
