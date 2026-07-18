package net.tanguydev.fraudservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.fraudservice.Domain.Enums.AlertStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_alerts", indexes = {
        @Index(name = "idx_fraud_alert_analysis_id", columnList = "fraud_analysis_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "fraud_analysis_id", nullable = false)
    private UUID fraudAnalysisId;

    @Column(name = "fraud_rule_id", nullable = false)
    private UUID fraudRuleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status", nullable = false)
    @Builder.Default
    private AlertStatus alertStatus = AlertStatus.OPEN;

    @Column(name = "score_at_trigger", precision = 5, scale = 2)
    private BigDecimal scoreAtTrigger;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @Column(name = "triggered_at", nullable = false)
    private OffsetDateTime triggeredAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        if (triggeredAt == null) triggeredAt = OffsetDateTime.now();
    }
}
