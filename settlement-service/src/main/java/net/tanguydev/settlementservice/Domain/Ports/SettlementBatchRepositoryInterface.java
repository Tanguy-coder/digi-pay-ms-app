package net.tanguydev.settlementservice.Domain.Ports;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementBatchRepositoryInterface {

    DomainSettlementBatch save(DomainSettlementBatch batch);

    Optional<DomainSettlementBatch> findById(UUID id);

    Optional<DomainSettlementBatch> findCurrentOpenBatch(String currency);

    List<DomainSettlementBatch> findByStatus(BatchStatus status);

    List<DomainSettlementBatch> findAll();
}
