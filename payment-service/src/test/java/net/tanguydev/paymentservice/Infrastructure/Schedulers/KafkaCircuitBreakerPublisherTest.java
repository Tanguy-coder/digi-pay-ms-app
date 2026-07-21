package net.tanguydev.paymentservice.Infrastructure.Schedulers;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Vérifie le comportement du Circuit Breaker et du Retry sur KafkaCircuitBreakerPublisher.
 *
 * On utilise @SpringBootTest pour que les annotations @CircuitBreaker/@Retry
 * soient traitées par le proxy Spring AOP (elles ne fonctionnent pas en test unitaire pur).
 */
@SpringBootTest
class KafkaCircuitBreakerPublisherTest {

    @MockitoBean
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    KafkaCircuitBreakerPublisher publisher;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        // Remet le circuit breaker à CLOSED avant chaque test
        circuitBreakerRegistry.circuitBreaker("kafka-publish").reset();
    }

    @Test
    void send_shouldSucceed_whenKafkaIsAvailable() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> publisher.send("payment-events", "key-1", "payload"));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void send_shouldRetry3Times_whenKafkaFails() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka unavailable")));

        assertThrows(Exception.class, () -> publisher.send("payment-events", "key-2", "payload"));

        // max-attempts=3 dans la config → KafkaTemplate appelé 3 fois
        verify(kafkaTemplate, times(3)).send(anyString(), anyString(), any());
    }

    @Test
    void circuitBreaker_shouldOpen_afterTooManyFailures() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka down")));

        // minimum-number-of-calls=5, failure-rate-threshold=50%
        // → 5 appels tous en échec → taux = 100% → circuit s'ouvre
        for (int i = 0; i < 5; i++) {
            try { publisher.send("payment-events", "key-" + i, "payload"); } catch (Exception ignored) {}
        }

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("kafka-publish");
        assertNotEquals(CircuitBreaker.State.CLOSED, cb.getState(),
                "Le circuit breaker aurait dû s'ouvrir après trop d'échecs");
    }
}
