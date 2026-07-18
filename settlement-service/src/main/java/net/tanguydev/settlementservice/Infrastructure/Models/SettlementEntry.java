package net.tanguydev.settlementservice.Infrastructure.Models;

import jakarta.persistence.*;
import lombok.*;
import net.tanguydev.settlementservice.Domain.Enums.EntryType;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "settlement_entries", indexes = {
        @Index(name = "idx_entry_settlement_id", columnList = "settlement_id"),
        @Index(name = "idx_entry_payment_id", columnList = "payment_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SettlementEntry {

    @Id
    private UUID id;

    @Column(name = "settlement_id", nullable = false)
    private UUID settlementId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "payment_reference", nullable = false, length = 100)
    private String paymentReference;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
}
