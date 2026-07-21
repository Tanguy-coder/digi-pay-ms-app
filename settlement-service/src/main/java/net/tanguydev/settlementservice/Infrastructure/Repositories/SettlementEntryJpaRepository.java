package net.tanguydev.settlementservice.Infrastructure.Repositories;

import net.tanguydev.settlementservice.Infrastructure.Models.SettlementEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementEntryJpaRepository extends JpaRepository<SettlementEntry, UUID> {

    List<SettlementEntry> findByBatchId(UUID batchId);

    boolean existsByPaymentId(UUID paymentId);
}
