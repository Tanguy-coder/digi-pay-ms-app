package net.tanguydev.settlementservice.Infrastructure.Producers;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEventPublisherInterface;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SettlementEventPublisher implements SettlementEventPublisherInterface {

    private static final String TOPIC = "settlement-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SettlementEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishBatchCompleted(DomainSettlementBatch batch) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "batch.completed");
        event.put("batchId", batch.getId().toString());
        event.put("reference", batch.getReference());
        event.put("status", batch.getStatus().name());
        event.put("totalEntries", batch.getTotalEntries());
        event.put("totalAmount", batch.getTotalAmount().toPlainString());
        event.put("currency", batch.getCurrency());
        event.put("settledAt", batch.getSettledAt().toString());
        kafkaTemplate.send(TOPIC, batch.getId().toString(), event);
    }

    @Override
    public void publishBatchFailed(DomainSettlementBatch batch, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "batch.failed");
        event.put("batchId", batch.getId().toString());
        event.put("reference", batch.getReference());
        event.put("reason", reason);
        kafkaTemplate.send(TOPIC, batch.getId().toString(), event);
    }
}
