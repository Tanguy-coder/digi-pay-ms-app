package net.tanguydev.paymentservice.Infrastructure.Producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.paymentservice.Domain.Events.PaymentEvent;
import net.tanguydev.paymentservice.Domain.Ports.PaymentEventPublisherInterface;
import net.tanguydev.paymentservice.Infrastructure.Models.OutboxEvent;
import net.tanguydev.paymentservice.Infrastructure.Repositories.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher implements PaymentEventPublisherInterface {

    private static final String TOPIC = "payment-events";

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentEventPublisher(OutboxEventJpaRepository outboxRepository,
                                 ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(PaymentEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("Payment")
                    .aggregateId(event.getPaymentId())
                    .eventType(event.getEventType())
                    .kafkaTopic(TOPIC)
                    .payload(payload)
                    .published(false)
                    .build();
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payment event for outbox", e);
        }
    }
}
