package net.tanguydev.settlementservice.Domain.Entities;

import net.tanguydev.settlementservice.Domain.Enums.NetPositionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class DomainNetPosition {

    private UUID id;
    private UUID batchId;
    private UUID walletId;
    private BigDecimal grossDebit;
    private BigDecimal grossCredit;
    private BigDecimal netAmount;
    private NetPositionStatus status;

    public DomainNetPosition() {
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

    public NetPositionStatus getStatus() {
        return status;
    }

    public void setStatus(NetPositionStatus status) {
        this.status = status;
    }
}
