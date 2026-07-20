package net.tanguydev.paymentservice.Infrastructure.Producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.paymentservice.Domain.Commands.WalletCommand;
import net.tanguydev.paymentservice.Domain.Ports.WalletCommandPublisherInterface;
import net.tanguydev.paymentservice.Infrastructure.Models.OutboxEvent;
import net.tanguydev.paymentservice.Infrastructure.Repositories.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class WalletCommandPublisher implements WalletCommandPublisherInterface {

    private static final String TOPIC = "wallet-commands";

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public WalletCommandPublisher(OutboxEventJpaRepository outboxRepository,
                                  ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(WalletCommand command) {
        try {
            String payload = objectMapper.writeValueAsString(command);
            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("Payment")
                    .aggregateId(command.getPaymentId())
                    .eventType(command.getCommandType())
                    .kafkaTopic(TOPIC)
                    .payload(payload)
                    .published(false)
                    .build();
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize wallet command for outbox", e);
        }
    }
}
