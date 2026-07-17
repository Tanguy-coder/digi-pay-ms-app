package net.tanguydev.paymentservice.Infrastructure.Producers;

import net.tanguydev.paymentservice.Domain.Commands.WalletCommand;
import net.tanguydev.paymentservice.Domain.Ports.WalletCommandPublisherInterface;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WalletCommandPublisher implements WalletCommandPublisherInterface {

    private static final String TOPIC = "wallet-commands";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public WalletCommandPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(WalletCommand command) {
        kafkaTemplate.send(TOPIC, command.getPaymentId().toString(), command);
    }
}
