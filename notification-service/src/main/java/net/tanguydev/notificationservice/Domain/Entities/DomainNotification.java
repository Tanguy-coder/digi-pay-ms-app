package net.tanguydev.notificationservice.Domain.Entities;

import net.tanguydev.notificationservice.Domain.Enums.NotificationStatus;
import net.tanguydev.notificationservice.Domain.Enums.NotificationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DomainNotification {

    private UUID id;
    private UUID walletId;
    private UUID paymentId;
    private NotificationType type;
    private NotificationStatus status;
    private String message;
    private BigDecimal amount;
    private String currency;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
