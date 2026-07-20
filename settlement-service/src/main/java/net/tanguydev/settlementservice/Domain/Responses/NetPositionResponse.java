package net.tanguydev.settlementservice.Domain.Responses;

import java.math.BigDecimal;
import java.util.UUID;

public class NetPositionResponse {

    private UUID id;
    private UUID walletId;
    private BigDecimal grossDebit;
    private BigDecimal grossCredit;
    private BigDecimal netAmount;
    private String status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
