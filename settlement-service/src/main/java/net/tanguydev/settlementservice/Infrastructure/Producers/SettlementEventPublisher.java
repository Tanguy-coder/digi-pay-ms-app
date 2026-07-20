package net.tanguydev.settlementservice.Infrastructure.Producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEventPublisherInterface;
import net.tanguydev.settlementservice.Infrastructure.Models.OutboxEvent;
import net.tanguydev.settlementservice.Infrastructure.Repositories.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SettlementEventPublisher implements SettlementEventPublisherInterface {

    private static final String TOPIC = "settlement-events";

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public SettlementEventPublisher(OutboxEventJpaRepository outboxRepository,
                                    ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishBatchCompleted(DomainSettlementBatch batch) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "batch.completed");
            event.put("batchId", batch.getId().toString());
            event.put("reference", batch.getReference());
            event.put("status", batch.getStatus().name());
            event.put("totalEntries", batch.getTotalEntries());
            event.put("totalAmount", batch.getTotalAmount().toPlainString());
            event.put("currency", batch.getCurrency());
            event.put("settledAt", batch.getSettledAt().toString());

            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outbox = new OutboxEvent(
                    "SettlementBatch",
                    batch.getId(),
                    "batch.completed",
                    TOPIC,
                    payload
            );
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize settlement event for outbox", e);
        }
    }

    @Override
    public void publishBatchFailed(DomainSettlementBatch batch, String reason) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "batch.failed");
            event.put("batchId", batch.getId().toString());
            event.put("reference", batch.getReference());
            event.put("reason", reason);

            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outbox = new OutboxEvent(
                    "SettlementBatch",
                    batch.getId(),
                    "batch.failed",
                    TOPIC,
                    payload
            );
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize settlement event for outbox", e);
        }
    }
}
