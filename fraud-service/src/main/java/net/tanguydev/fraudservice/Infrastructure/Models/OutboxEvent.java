package net.tanguydev.fraudservice.Infrastructure.Models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_published", columnList = "published")
})
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "aggregate_type", length = 100, nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;

    @Column(name = "kafka_topic", length = 200, nullable = false)
    private String kafkaTopic;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "published", nullable = false)
    private boolean published = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = OffsetDateTime.now();
    }

    public OutboxEvent() {}

    public OutboxEvent(String aggregateType, UUID aggregateId, String eventType,
                       String kafkaTopic, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.kafkaTopic = kafkaTopic;
        this.payload = payload;
        this.published = false;
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getKafkaTopic() { return kafkaTopic; }
    public String getPayload() { return payload; }
    public boolean isPublished() { return published; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }

    public void setPublished(boolean published) { this.published = published; }
    public void setPublishedAt(OffsetDateTime publishedAt) { this.publishedAt = publishedAt; }
}
