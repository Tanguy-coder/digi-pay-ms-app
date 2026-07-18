package net.tanguydev.fraudservice.Infrastructure.Producers;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Ports.FraudEventPublisherInterface;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FraudEventPublisher implements FraudEventPublisherInterface {

    private static final String TOPIC = "fraud-check-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FraudEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainFraudAnalysis analysis) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", resolveEventType(analysis));
        event.put("paymentId", analysis.getPaymentId().toString());
        event.put("customerId", analysis.getCustomerId().toString());
        event.put("verdict", analysis.getVerdict().name());
        event.put("riskScore", analysis.getRiskScore().toPlainString());
        kafkaTemplate.send(TOPIC, analysis.getPaymentId().toString(), event);
    }

    private String resolveEventType(DomainFraudAnalysis analysis) {
        return switch (analysis.getVerdict()) {
            case CLEARED         -> "fraud.cleared";
            case REVIEW, FLAGGED -> "fraud.review";
            case BLOCKED         -> "fraud.blocked";
        };
    }
}
