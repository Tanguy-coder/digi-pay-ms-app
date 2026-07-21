package net.tanguydev.paymentservice.Infrastructure.Schedulers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.paymentservice.Infrastructure.Models.OutboxEvent;
import net.tanguydev.paymentservice.Infrastructure.Repositories.OutboxEventJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaCircuitBreakerPublisher kafkaPublisher;
    private final ObjectMapper objectMapper;

    public OutboxRelay(OutboxEventJpaRepository outboxRepository,
                       KafkaCircuitBreakerPublisher kafkaPublisher,
                       ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaPublisher = kafkaPublisher;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.poll-interval-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent event : events) {
            try {
                Object payload = objectMapper.readValue(event.getPayload(), Object.class);
                kafkaPublisher.send(event.getKafkaTopic(), event.getAggregateId().toString(), payload);
                event.setPublished(true);
                event.setPublishedAt(OffsetDateTime.now());
                outboxRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                break;
            }
        }
    }
}
