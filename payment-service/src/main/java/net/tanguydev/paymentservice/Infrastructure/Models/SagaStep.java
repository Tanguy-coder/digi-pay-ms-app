package net.tanguydev.paymentservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.paymentservice.Domain.Enums.SagaStepName;
import net.tanguydev.paymentservice.Domain.Enums.SagaStepStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "saga_steps", indexes = {
        @Index(name = "idx_saga_step_payment_id", columnList = "payment_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SagaStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_name", nullable = false)
    private SagaStepName stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_status", nullable = false)
    @Builder.Default
    private SagaStepStatus stepStatus = SagaStepStatus.PENDING;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "compensation_event", length = 100)
    private String compensationEvent;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private int maxRetries = 3;

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}
