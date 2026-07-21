package net.tanguydev.settlementservice.Infrastructure.EventStore;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.settlementservice.Domain.Enums.BatchEventType;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.NetPositionStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_events", indexes = {
        @Index(name = "idx_batch_events_batch_id", columnList = "batch_id"),
        @Index(name = "idx_batch_events_batch_version", columnList = "batch_id, aggregate_version", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false)
    private UUID id;

    @Column(name = "batch_id", nullable = false, updatable = false)
    private UUID batchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    private BatchEventType eventType;

    @Column(name = "aggregate_version", nullable = false, updatable = false)
    private Long aggregateVersion;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    @Column(updatable = false)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private SettlementCycle cycle;

    @Column(length = 3, updatable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private BatchStatus status;

    @Column(name = "payment_id", updatable = false)
    private UUID paymentId;

    @Column(name = "payment_reference", updatable = false)
    private String paymentReference;

    @Column(name = "sender_wallet_id", updatable = false)
    private UUID senderWalletId;

    @Column(name = "receiver_wallet_id", updatable = false)
    private UUID receiverWalletId;

    @Column(precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;

    @Column(name = "wallet_id", updatable = false)
    private UUID walletId;

    @Column(name = "gross_debit", precision = 19, scale = 4, updatable = false)
    private BigDecimal grossDebit;

    @Column(name = "gross_credit", precision = 19, scale = 4, updatable = false)
    private BigDecimal grossCredit;

    @Column(name = "net_amount", precision = 19, scale = 4, updatable = false)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_status", updatable = false)
    private NetPositionStatus positionStatus;

    @Column(updatable = false)
    private String reason;
}
