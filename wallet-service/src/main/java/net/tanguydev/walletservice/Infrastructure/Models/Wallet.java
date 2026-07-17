package net.tanguydev.walletservice.Infrastructure.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets",
        indexes = {
                @Index(name = "idx_wallet_customer_id", columnList = "customer_id"),
                @Index(name = "idx_wallet_number", columnList = "wallet_number", unique = true)
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_wallet_customer_type", columnNames = {"customer_id", "wallet_type"})
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @NotBlank
    @Column(name = "wallet_number", length = 30, nullable = false, unique = true, updatable = false)
    private String walletNumber;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false)
    @Builder.Default
    private WalletType walletType = WalletType.PERSONAL;

    @NotBlank @Size(min = 3, max = 3)
    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "XOF";

    @NotNull @DecimalMin("0.00")
    @Column(name = "balance", precision = 19, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull @DecimalMin("0.00")
    @Column(name = "frozen_amount", precision = 19, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    @Column(name = "daily_limit", precision = 19, scale = 4)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 19, scale = 4)
    private BigDecimal monthlyLimit;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
