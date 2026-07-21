package net.tanguydev.fraudservice.Infrastructure.Producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Ports.FraudEventPublisherInterface;
import net.tanguydev.fraudservice.Infrastructure.Models.OutboxEvent;
import net.tanguydev.fraudservice.Infrastructure.Repositories.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FraudEventPublisher implements FraudEventPublisherInterface {

    private static final String TOPIC = "fraud-check-events";

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public FraudEventPublisher(OutboxEventJpaRepository outboxRepository,
                               ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainFraudAnalysis analysis) {
        try {
            String eventType = resolveEventType(analysis);
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("paymentId", analysis.getPaymentId().toString());
            event.put("customerId", analysis.getCustomerId().toString());
            event.put("verdict", analysis.getVerdict().name());
            event.put("riskScore", analysis.getRiskScore().toPlainString());

            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outbox = new OutboxEvent(
                    "FraudAnalysis",
                    analysis.getPaymentId(),
                    eventType,
                    TOPIC,
                    payload
            );
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize fraud event for outbox", e);
        }
    }

    private String resolveEventType(DomainFraudAnalysis analysis) {
        return switch (analysis.getVerdict()) {
            case CLEARED         -> "fraud.cleared";
            case REVIEW, FLAGGED -> "fraud.review";
            case BLOCKED         -> "fraud.blocked";
        };
    }
}
