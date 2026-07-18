package net.tanguydev.fraudservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_risk_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerRiskProfile {

    @Id
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "lifetime_risk_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal lifetimeRiskScore = BigDecimal.ZERO;

    @Column(name = "total_alerts", nullable = false)
    @Builder.Default
    private Integer totalAlerts = 0;

    @Column(name = "false_positives", nullable = false)
    @Builder.Default
    private Integer falsePositives = 0;

    @Column(name = "is_blacklisted", nullable = false)
    @Builder.Default
    private boolean blacklisted = false;

    @Column(name = "avg_transaction_amount", precision = 20, scale = 6)
    @Builder.Default
    private BigDecimal avgTransactionAmount = BigDecimal.ZERO;

    @Column(name = "last_analysis_at")
    private OffsetDateTime lastAnalysisAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
