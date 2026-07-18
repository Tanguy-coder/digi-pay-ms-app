package net.tanguydev.settlementservice.Infrastructure.Producers;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
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
    public void publishSettlementCompleted(DomainSettlement settlement) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "settlement.completed");
        event.put("settlementId", settlement.getId().toString());
        event.put("reference", settlement.getReference());
        event.put("status", settlement.getStatus().name());
        event.put("totalPayments", settlement.getTotalPayments());
        event.put("totalAmount", settlement.getTotalAmount().toPlainString());
        event.put("netPosition", settlement.getNetPosition().toPlainString());
        event.put("currency", settlement.getCurrency());
        event.put("settledAt", settlement.getSettledAt().toString());
        kafkaTemplate.send(TOPIC, settlement.getId().toString(), event);
    }

    @Override
    public void publishSettlementFailed(DomainSettlement settlement, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "settlement.failed");
        event.put("settlementId", settlement.getId().toString());
        event.put("reference", settlement.getReference());
        event.put("reason", reason);
        kafkaTemplate.send(TOPIC, settlement.getId().toString(), event);
    }
}
