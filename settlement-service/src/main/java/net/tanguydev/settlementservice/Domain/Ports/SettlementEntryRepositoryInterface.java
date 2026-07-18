package net.tanguydev.settlementservice.Domain.Ports;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;

import java.util.List;
import java.util.UUID;

public interface SettlementEntryRepositoryInterface {
    DomainSettlementEntry save(DomainSettlementEntry entry);
    List<DomainSettlementEntry> findBySettlementId(UUID settlementId);
    boolean existsByPaymentId(UUID paymentId);
}
