package net.tanguydev.paymentservice.Infrastructure.Producers;

import net.tanguydev.paymentservice.Domain.Events.PaymentEvent;
import net.tanguydev.paymentservice.Domain.Ports.PaymentEventPublisherInterface;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher implements PaymentEventPublisherInterface {

    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(PaymentEvent event) {
        kafkaTemplate.send(TOPIC, event.getPaymentId().toString(), event);
    }
}
