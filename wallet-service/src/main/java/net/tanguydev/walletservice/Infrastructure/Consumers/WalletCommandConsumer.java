package net.tanguydev.walletservice.Infrastructure.Consumers;

import net.tanguydev.walletservice.Domain.UseCases.CreditWalletUseCaseInterface;
import net.tanguydev.walletservice.Domain.UseCases.DebitWalletUseCaseInterface;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reçoit les commandes Saga du payment-service (topic: wallet-commands).
 * Exécute l'opération demandée (DEBIT / CREDIT / COMPENSATE_DEBIT)
 * puis répond sur le topic wallet-saga-events avec succès ou échec.
 */
@Component
public class WalletCommandConsumer {

    private static final String SAGA_REPLY_TOPIC = "wallet-saga-events";

    private final DebitWalletUseCaseInterface debitWallet;
    private final CreditWalletUseCaseInterface creditWallet;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public WalletCommandConsumer(DebitWalletUseCaseInterface debitWallet,
                                 CreditWalletUseCaseInterface creditWallet,
                                 KafkaTemplate<String, Object> kafkaTemplate) {
        this.debitWallet = debitWallet;
        this.creditWallet = creditWallet;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "wallet-commands", groupId = "wallet-saga-group")
    public void consume(Map<String, Object> message) {
        String commandType = (String) message.get("commandType");
        UUID paymentId = UUID.fromString((String) message.get("paymentId"));
        UUID walletId = UUID.fromString((String) message.get("walletId"));
        BigDecimal amount = new BigDecimal(message.get("amount").toString());

        switch (commandType) {
            case "DEBIT"           -> handleDebit(paymentId, walletId, amount);
            case "CREDIT"          -> handleCredit(paymentId, walletId, amount);
            case "COMPENSATE_DEBIT" -> handleCompensateDebit(paymentId, walletId, amount);
            default -> {}
        }
    }

    // ── handlers ──────────────────────────────────────────────────────────────

    private void handleDebit(UUID paymentId, UUID walletId, BigDecimal amount) {
        try {
            debitWallet.execute(walletId, amount);
            reply(paymentId, "wallet.debit.success", null);
        } catch (Exception e) {
            reply(paymentId, "wallet.debit.failed", e.getMessage());
        }
    }

    private void handleCredit(UUID paymentId, UUID walletId, BigDecimal amount) {
        try {
            creditWallet.execute(walletId, amount);
            reply(paymentId, "wallet.credit.success", null);
        } catch (Exception e) {
            reply(paymentId, "wallet.credit.failed", e.getMessage());
        }
    }

    private void handleCompensateDebit(UUID paymentId, UUID walletId, BigDecimal amount) {
        try {
            creditWallet.execute(walletId, amount);
            reply(paymentId, "wallet.compensation.success", null);
        } catch (Exception e) {
            reply(paymentId, "wallet.compensation.failed", e.getMessage());
        }
    }

    private void reply(UUID paymentId, String eventType, String reason) {
        Map<String, Object> response = new HashMap<>();
        response.put("eventType", eventType);
        response.put("paymentId", paymentId.toString());
        if (reason != null) response.put("reason", reason);
        kafkaTemplate.send(SAGA_REPLY_TOPIC, paymentId.toString(), response);
    }
}
