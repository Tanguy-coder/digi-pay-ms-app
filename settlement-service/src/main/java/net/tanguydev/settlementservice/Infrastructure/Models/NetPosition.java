package net.tanguydev.settlementservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.settlementservice.Domain.Enums.NetPositionStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "net_positions", indexes = {
        @Index(name = "idx_position_batch_id", columnList = "batch_id"),
        @Index(name = "idx_position_batch_wallet", columnList = "batch_id, wallet_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetPosition {

    @Id
    private UUID id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "gross_debit", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossDebit;

    @Column(name = "gross_credit", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossCredit;

    @Column(name = "net_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NetPositionStatus status;
}
