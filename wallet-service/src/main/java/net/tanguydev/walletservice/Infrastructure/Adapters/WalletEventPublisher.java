package net.tanguydev.walletservice.Infrastructure.Adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Infrastructure.Models.OutboxEvent;
import net.tanguydev.walletservice.Infrastructure.Repositories.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class WalletEventPublisher implements WalletEventPublisherInterface {

    private static final String TOPIC = "wallet-events";

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public WalletEventPublisher(OutboxEventJpaRepository outboxRepository,
                                ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(WalletEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outbox = new OutboxEvent(
                    "Wallet",
                    event.getWalletId(),
                    event.getEventType(),
                    TOPIC,
                    payload
            );
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize wallet event for outbox", e);
        }
    }
}
