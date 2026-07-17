package net.tanguydev.paymentservice.Infrastructure.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Enums.PaymentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_sender_wallet", columnList = "sender_wallet_id"),
        @Index(name = "idx_payment_receiver_wallet", columnList = "receiver_wallet_id"),
        @Index(name = "idx_payment_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_payment_reference", columnList = "payment_reference", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "payment_reference", length = 40, nullable = false, unique = true, updatable = false)
    private String paymentReference;

    @NotNull
    @Column(name = "sender_wallet_id", nullable = false)
    private UUID senderWalletId;

    @NotNull
    @Column(name = "receiver_wallet_id", nullable = false)
    private UUID receiverWalletId;

    @NotNull @DecimalMin("0.01")
    @Column(name = "amount", precision = 20, scale = 6, nullable = false)
    private BigDecimal amount;

    @Column(name = "fee_amount", precision = 20, scale = 6)
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "exchange_rate", precision = 15, scale = 6)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.INITIATED;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType type;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "idempotency_key", length = 100, nullable = false, unique = true, updatable = false)
    private String idempotencyKey;

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "initiated_at", nullable = false, updatable = false)
    private OffsetDateTime initiatedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (this.initiatedAt == null) this.initiatedAt = OffsetDateTime.now();
    }
}
