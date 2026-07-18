package net.tanguydev.fraudservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.fraudservice.Domain.Enums.FraudVerdict;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_analyses", indexes = {
        @Index(name = "idx_fraud_analysis_payment_id", columnList = "payment_id", unique = true),
        @Index(name = "idx_fraud_analysis_customer_id", columnList = "customer_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FraudAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true)
    private UUID paymentId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "risk_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", nullable = false)
    private FraudVerdict verdict;

    @Column(name = "analysis_duration_ms")
    private Integer analysisDurationMs;

    @Column(name = "rules_triggered", columnDefinition = "TEXT")
    private String rulesTriggered;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;

    @Column(name = "is_new_device")
    private Boolean isNewDevice;

    @Column(name = "transaction_hour")
    private Integer transactionHour;

    @Column(name = "velocity_1min")
    private Integer velocity1min;

    @Column(name = "velocity_1h")
    private Integer velocity1h;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
