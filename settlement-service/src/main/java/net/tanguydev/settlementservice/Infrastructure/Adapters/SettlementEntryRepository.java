package net.tanguydev.settlementservice.Infrastructure.Adapters;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Infrastructure.Mappers.SettlementMapper;
import net.tanguydev.settlementservice.Infrastructure.Repositories.SettlementEntryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SettlementEntryRepository implements SettlementEntryRepositoryInterface {

    private final SettlementEntryJpaRepository jpa;
    private final SettlementMapper mapper;

    public SettlementEntryRepository(SettlementEntryJpaRepository jpa, SettlementMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public DomainSettlementEntry save(DomainSettlementEntry entry) {
        return mapper.toEntryDomain(jpa.save(mapper.toEntryJpa(entry)));
    }

    @Override
    public List<DomainSettlementEntry> findBySettlementId(UUID settlementId) {
        return mapper.toEntryDomainList(jpa.findBySettlementId(settlementId));
    }

    @Override
    public boolean existsByPaymentId(UUID paymentId) {
        return jpa.existsByPaymentId(paymentId);
    }
}
