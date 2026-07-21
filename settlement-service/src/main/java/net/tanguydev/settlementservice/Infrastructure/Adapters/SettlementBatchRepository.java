package net.tanguydev.settlementservice.Infrastructure.Adapters;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Infrastructure.Mappers.SettlementMapper;
import net.tanguydev.settlementservice.Infrastructure.Models.SettlementBatch;
import net.tanguydev.settlementservice.Infrastructure.Repositories.SettlementBatchJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SettlementBatchRepository implements SettlementBatchRepositoryInterface {

    private final SettlementBatchJpaRepository jpaRepository;
    private final SettlementMapper mapper;

    public SettlementBatchRepository(SettlementBatchJpaRepository jpaRepository, SettlementMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainSettlementBatch save(DomainSettlementBatch batch) {
        SettlementBatch entity = mapper.toBatchJpa(batch);
        SettlementBatch saved = jpaRepository.save(entity);
        return mapper.toBatchDomain(saved);
    }

    @Override
    public Optional<DomainSettlementBatch> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toBatchDomain);
    }

    @Override
    public Optional<DomainSettlementBatch> findCurrentOpenBatch(String currency) {
        List<BatchStatus> openStatuses = List.of(BatchStatus.OPEN, BatchStatus.COLLECTING);
        return jpaRepository.findFirstByStatusInAndCurrencyOrderByOpenedAtDesc(openStatuses, currency)
                .map(mapper::toBatchDomain);
    }

    @Override
    public List<DomainSettlementBatch> findByStatus(BatchStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toBatchDomain)
                .toList();
    }

    @Override
    public List<DomainSettlementBatch> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toBatchDomain)
                .toList();
    }
}
