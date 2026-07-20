package net.tanguydev.settlementservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement_batches", indexes = {
        @Index(name = "idx_batch_reference", columnList = "reference", unique = true),
        @Index(name = "idx_batch_status", columnList = "status"),
        @Index(name = "idx_batch_status_currency", columnList = "status, currency")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementBatch {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementCycle cycle;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "total_entries", nullable = false)
    private int totalEntries;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
