package net.tanguydev.paymentservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "idempotency_keys")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IdempotencyKey {

    @Id
    @Column(name = "key", length = 100, nullable = false)
    private String key;

    @Column(name = "response_status", nullable = false)
    private int responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = OffsetDateTime.now();
    }
}
