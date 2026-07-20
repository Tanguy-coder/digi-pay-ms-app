package net.tanguydev.customerservice.Infrastructure.Adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.customerservice.Domain.Events.CustomerEvent;
import net.tanguydev.customerservice.Domain.Ports.CustomerEventPublisherInterface;
import net.tanguydev.customerservice.Infrastructure.Models.OutboxEvent;
import net.tanguydev.customerservice.Infrastructure.Repositories.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventPublisher implements CustomerEventPublisherInterface {

    private static final String TOPIC = "customer-events";

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public CustomerEventPublisher(OutboxEventJpaRepository outboxRepository,
                                  ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(CustomerEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outbox = new OutboxEvent(
                    "Customer",
                    event.getCustomerId(),
                    event.getEventType(),
                    TOPIC,
                    payload
            );
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize customer event for outbox", e);
        }
    }
}
