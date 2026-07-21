package net.tanguydev.settlementservice.Infrastructure.Adapters;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Infrastructure.Mappers.SettlementMapper;
import net.tanguydev.settlementservice.Infrastructure.Models.SettlementEntry;
import net.tanguydev.settlementservice.Infrastructure.Repositories.SettlementEntryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SettlementEntryRepository implements SettlementEntryRepositoryInterface {

    private final SettlementEntryJpaRepository jpaRepository;
    private final SettlementMapper mapper;

    public SettlementEntryRepository(SettlementEntryJpaRepository jpaRepository, SettlementMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainSettlementEntry save(DomainSettlementEntry entry) {
        SettlementEntry entity = mapper.toEntryJpa(entry);
        SettlementEntry saved = jpaRepository.save(entity);
        return mapper.toEntryDomain(saved);
    }

    @Override
    public List<DomainSettlementEntry> findByBatchId(UUID batchId) {
        return jpaRepository.findByBatchId(batchId).stream()
                .map(mapper::toEntryDomain)
                .toList();
    }

    @Override
    public boolean existsByPaymentId(UUID paymentId) {
        return jpaRepository.existsByPaymentId(paymentId);
    }
}
