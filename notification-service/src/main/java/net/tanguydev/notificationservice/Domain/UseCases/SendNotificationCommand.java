package net.tanguydev.notificationservice.Domain.UseCases;

import net.tanguydev.notificationservice.Domain.Enums.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;

public class SendNotificationCommand {

    private UUID walletId;
    private UUID paymentId;
    private NotificationType type;
    private BigDecimal amount;
    private String currency;

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
