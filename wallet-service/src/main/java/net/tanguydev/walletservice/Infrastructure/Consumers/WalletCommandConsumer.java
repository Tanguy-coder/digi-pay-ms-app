package net.tanguydev.walletservice.Infrastructure.Consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.walletservice.Domain.UseCases.CreditWalletUseCaseInterface;
import net.tanguydev.walletservice.Domain.UseCases.DebitWalletUseCaseInterface;
import net.tanguydev.walletservice.Infrastructure.Models.OutboxEvent;
import net.tanguydev.walletservice.Infrastructure.Repositories.OutboxEventJpaRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class WalletCommandConsumer {

    private static final String SAGA_REPLY_TOPIC = "wallet-saga-events";

    private final DebitWalletUseCaseInterface debitWallet;
    private final CreditWalletUseCaseInterface creditWallet;
    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public WalletCommandConsumer(DebitWalletUseCaseInterface debitWallet,
                                 CreditWalletUseCaseInterface creditWallet,
                                 OutboxEventJpaRepository outboxRepository,
                                 ObjectMapper objectMapper) {
        this.debitWallet = debitWallet;
        this.creditWallet = creditWallet;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "wallet-commands", groupId = "wallet-saga-group")
    @Transactional
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
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("eventType", eventType);
            response.put("paymentId", paymentId.toString());
            if (reason != null) response.put("reason", reason);

            String payload = objectMapper.writeValueAsString(response);
            OutboxEvent outbox = new OutboxEvent(
                    "WalletSaga",
                    paymentId,
                    eventType,
                    SAGA_REPLY_TOPIC,
                    payload
            );
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize saga reply for outbox", e);
        }
    }
}
