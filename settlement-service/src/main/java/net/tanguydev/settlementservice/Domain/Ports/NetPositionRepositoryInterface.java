package net.tanguydev.settlementservice.Domain.Ports;

import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;

import java.util.List;
import java.util.UUID;

public interface NetPositionRepositoryInterface {

    DomainNetPosition save(DomainNetPosition position);

    List<DomainNetPosition> saveAll(List<DomainNetPosition> positions);

    List<DomainNetPosition> findByBatchId(UUID batchId);
}
