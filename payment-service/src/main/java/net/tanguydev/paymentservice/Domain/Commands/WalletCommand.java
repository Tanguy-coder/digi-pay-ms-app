package net.tanguydev.paymentservice.Domain.Commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Commande envoyée au wallet-service via Kafka (topic: wallet-commands).
 * Le wallet-service lit cette commande, exécute l'opération, puis répond
 * sur le topic wallet-saga-events.
 *
 * Types de commandes :
 *  - DEBIT          : débiter le wallet emetteur
 *  - CREDIT         : créditer le wallet destinataire
 *  - COMPENSATE_DEBIT : re-créditer le wallet emetteur (annulation du débit)
 */
public class WalletCommand {

    private String commandType;
    private UUID paymentId;
    private UUID walletId;
    private BigDecimal amount;
    private String currency;

    public WalletCommand() {}

    public String getCommandType() { return commandType; }
    public void setCommandType(String commandType) { this.commandType = commandType; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
