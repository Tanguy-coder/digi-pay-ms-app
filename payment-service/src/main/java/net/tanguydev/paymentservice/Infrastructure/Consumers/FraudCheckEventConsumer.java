package net.tanguydev.paymentservice.Infrastructure.Consumers;

import net.tanguydev.paymentservice.Domain.UseCases.PaymentSagaOrchestratorInterface;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Écoute fraud-check-events.
 * Le fraud-service y publie le verdict pour chaque paiement analysé.
 */
@Component
public class FraudCheckEventConsumer {

    private final PaymentSagaOrchestratorInterface orchestrator;

    public FraudCheckEventConsumer(PaymentSagaOrchestratorInterface orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = "fraud-check-events", groupId = "payment-fraud-group")
    public void consume(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        UUID paymentId = UUID.fromString((String) message.get("paymentId"));
        String verdict = (String) message.getOrDefault("verdict", null);

        switch (eventType) {
            case "fraud.cleared" -> orchestrator.onFraudCleared(paymentId);
            case "fraud.blocked" -> orchestrator.onFraudBlocked(paymentId, "verdict=" + verdict);
            default -> {}
        }
    }
}
