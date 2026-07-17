package net.tanguydev.paymentservice.Domain.Entities;

import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Enums.PaymentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DomainPayment {

    private UUID id;
    private String paymentReference;
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private String currency;
    private BigDecimal exchangeRate;
    private PaymentStatus status;
    private PaymentType type;
    private String failureReason;
    private String description;
    private String idempotencyKey;
    private UUID merchantId;
    private String metadata;
    private Long version;
    private OffsetDateTime initiatedAt;
    private OffsetDateTime completedAt;

    public DomainPayment() {}

    public BigDecimal getNetAmount() {
        if (amount == null) return null;
        BigDecimal fee = feeAmount != null ? feeAmount : BigDecimal.ZERO;
        return amount.subtract(fee);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public UUID getSenderWalletId() { return senderWalletId; }
    public void setSenderWalletId(UUID senderWalletId) { this.senderWalletId = senderWalletId; }

    public UUID getReceiverWalletId() { return receiverWalletId; }
    public void setReceiverWalletId(UUID receiverWalletId) { this.receiverWalletId = receiverWalletId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public PaymentType getType() { return type; }
    public void setType(PaymentType type) { this.type = type; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public UUID getMerchantId() { return merchantId; }
    public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public OffsetDateTime getInitiatedAt() { return initiatedAt; }
    public void setInitiatedAt(OffsetDateTime initiatedAt) { this.initiatedAt = initiatedAt; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
