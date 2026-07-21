package net.tanguydev.paymentservice.Infrastructure.Schedulers;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaCircuitBreakerPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaCircuitBreakerPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @CircuitBreaker(name = "kafka-publish")
    @Retry(name = "kafka-publish")
    public void send(String topic, String key, Object payload) throws Exception {
        kafkaTemplate.send(topic, key, payload).get();
    }
}
