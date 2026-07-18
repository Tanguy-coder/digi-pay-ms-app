package net.tanguydev.fraudservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.fraudservice.Domain.Enums.RuleAction;
import net.tanguydev.fraudservice.Domain.Enums.RulePriority;
import net.tanguydev.fraudservice.Domain.Enums.RuleType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_code", length = 50, nullable = false, unique = true)
    private String ruleCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @Column(name = "threshold_value", precision = 20, scale = 6)
    private BigDecimal thresholdValue;

    @Column(name = "score_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal scoreWeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private RuleAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private RulePriority priority;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
