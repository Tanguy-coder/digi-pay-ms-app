package net.tanguydev.settlementservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Enums.SettlementStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements", indexes = {
        @Index(name = "idx_settlement_reference", columnList = "reference", unique = true),
        @Index(name = "idx_settlement_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Settlement {

    @Id
    private UUID id;

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "cycle", nullable = false)
    private SettlementCycle cycle;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "total_payments", nullable = false)
    private int totalPayments;

    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "net_position", precision = 19, scale = 4, nullable = false)
    private BigDecimal netPosition;

    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
