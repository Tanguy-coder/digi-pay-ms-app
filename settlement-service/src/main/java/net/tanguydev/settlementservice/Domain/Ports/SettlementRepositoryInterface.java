package net.tanguydev.settlementservice.Domain.Ports;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
import net.tanguydev.settlementservice.Domain.Enums.SettlementStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepositoryInterface {
    DomainSettlement save(DomainSettlement settlement);
    Optional<DomainSettlement> findById(UUID id);
    Optional<DomainSettlement> findByReference(String reference);
    List<DomainSettlement> findByStatus(SettlementStatus status);
    List<DomainSettlement> findAll();
}
