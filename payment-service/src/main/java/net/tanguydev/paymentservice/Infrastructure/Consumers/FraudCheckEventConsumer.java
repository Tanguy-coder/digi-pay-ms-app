package net.tanguydev.paymentservice.Infrastructure.Consumers;

import io.micrometer.observation.annotation.Observed;
import net.tanguydev.paymentservice.Domain.UseCases.PaymentSagaOrchestratorInterface;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@Observed(name = "payment.fraud-check.consumer")
public class FraudCheckEventConsumer {

    private final PaymentSagaOrchestratorInterface orchestrator;

    public FraudCheckEventConsumer(PaymentSagaOrchestratorInterface orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = "fraud-check-events", groupId = "payment-fraud-group")
    @Transactional
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
