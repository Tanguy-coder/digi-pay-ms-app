package net.tanguydev.customerservice.Infrastructure.Adapters;

import net.tanguydev.customerservice.Domain.Events.CustomerEvent;
import net.tanguydev.customerservice.Domain.Ports.CustomerEventPublisherInterface;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventPublisher implements CustomerEventPublisherInterface {
    private static final String TOPIC = "customer-events";
    private final KafkaTemplate<String, CustomerEvent> kafkaTemplate;

    public CustomerEventPublisher(KafkaTemplate<String, CustomerEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(CustomerEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.getCustomerId()), event);
    }
}
