package net.tanguydev.paymentservice.Domain.Events;

import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Enums.PaymentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PaymentEvent {

    private String eventType;
    private UUID paymentId;
    private String paymentReference;
    private UUID senderWalletId;
    private UUID receiverWalletId;
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private String currency;
    private PaymentStatus status;
    private PaymentType type;
    private String idempotencyKey;
    private OffsetDateTime occurredAt;

    public PaymentEvent() {}

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

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

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public PaymentType getType() { return type; }
    public void setType(PaymentType type) { this.type = type; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
}
