package net.tanguydev.walletservice.Infrastructure.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_customer_id", columnList = "customer_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @NotBlank @Size(min = 3, max = 3)
    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "XOF";

    @NotNull @DecimalMin("0.00")
    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull @DecimalMin("0.00")
    @Column(name = "frozen_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

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
