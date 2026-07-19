package net.tanguydev.walletservice.Infrastructure.EventStore;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.walletservice.Domain.Enums.WalletEventType;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_events",
        indexes = {
                @Index(name = "idx_wallet_events_wallet_id", columnList = "wallet_id"),
                @Index(name = "idx_wallet_events_wallet_version", columnList = "wallet_id, aggregate_version", unique = true)
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    private WalletEventType eventType;

    @Column(name = "customer_id", updatable = false)
    private UUID customerId;

    @Column(name = "currency", length = 3, updatable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", updatable = false)
    private WalletType walletType;

    @Column(name = "wallet_number", updatable = false)
    private String walletNumber;

    @Column(name = "amount", precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;

    @Column(name = "daily_limit", precision = 19, scale = 4, updatable = false)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 19, scale = 4, updatable = false)
    private BigDecimal monthlyLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", updatable = false)
    private WalletStatus status;

    @Column(name = "aggregate_version", nullable = false, updatable = false)
    private Long aggregateVersion;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;
}
