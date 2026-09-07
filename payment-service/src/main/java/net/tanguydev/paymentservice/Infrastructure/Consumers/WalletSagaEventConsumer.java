package net.tanguydev.paymentservice.Infrastructure.Consumers;

import io.micrometer.observation.annotation.Observed;
import net.tanguydev.paymentservice.Domain.UseCases.PaymentSagaOrchestratorInterface;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@Observed(name = "payment.wallet-saga.consumer")
public class WalletSagaEventConsumer {

    private final PaymentSagaOrchestratorInterface orchestrator;

    public WalletSagaEventConsumer(PaymentSagaOrchestratorInterface orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = "wallet-saga-events", groupId = "payment-saga-group")
    @Transactional
    public void consume(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        UUID paymentId = UUID.fromString((String) message.get("paymentId"));
        String reason = (String) message.getOrDefault("reason", null);

        switch (eventType) {
            case "wallet.debit.success"        -> orchestrator.onDebitSuccess(paymentId);
            case "wallet.debit.failed"         -> orchestrator.onDebitFailure(paymentId, reason);
            case "wallet.credit.success"       -> orchestrator.onCreditSuccess(paymentId);
            case "wallet.credit.failed"        -> orchestrator.onCreditFailure(paymentId, reason);
            case "wallet.compensation.success" -> orchestrator.onCompensationCompleted(paymentId);
            case "wallet.compensation.failed"  -> orchestrator.onCompensationFailed(paymentId, reason);
            default -> {}
        }
    }
}
