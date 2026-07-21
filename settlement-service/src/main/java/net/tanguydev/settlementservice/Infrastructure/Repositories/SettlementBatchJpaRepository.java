package net.tanguydev.settlementservice.Infrastructure.Repositories;

import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Infrastructure.Models.SettlementBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementBatchJpaRepository extends JpaRepository<SettlementBatch, UUID> {

    Optional<SettlementBatch> findFirstByStatusInAndCurrencyOrderByOpenedAtDesc(List<BatchStatus> statuses, String currency);

    List<SettlementBatch> findByStatus(BatchStatus status);
}
