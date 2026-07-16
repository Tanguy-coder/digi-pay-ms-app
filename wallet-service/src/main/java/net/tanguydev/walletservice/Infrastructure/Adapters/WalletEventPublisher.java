package net.tanguydev.walletservice.Infrastructure.Adapters;

import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WalletEventPublisher implements WalletEventPublisherInterface {

    private static final String TOPIC = "wallet-events";
    private final KafkaTemplate<String, WalletEvent> kafkaTemplate;

    public WalletEventPublisher(KafkaTemplate<String, WalletEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(WalletEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.getWalletId()), event);
    }
}
