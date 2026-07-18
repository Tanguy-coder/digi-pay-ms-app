package net.tanguydev.settlementservice.Infrastructure.Adapters;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
import net.tanguydev.settlementservice.Domain.Enums.SettlementStatus;
import net.tanguydev.settlementservice.Domain.Ports.SettlementRepositoryInterface;
import net.tanguydev.settlementservice.Infrastructure.Mappers.SettlementMapper;
import net.tanguydev.settlementservice.Infrastructure.Repositories.SettlementJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SettlementRepository implements SettlementRepositoryInterface {

    private final SettlementJpaRepository jpa;
    private final SettlementMapper mapper;

    public SettlementRepository(SettlementJpaRepository jpa, SettlementMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public DomainSettlement save(DomainSettlement settlement) {
        return mapper.toDomain(jpa.save(mapper.toJpa(settlement)));
    }

    @Override
    public Optional<DomainSettlement> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<DomainSettlement> findByReference(String reference) {
        return jpa.findByReference(reference).map(mapper::toDomain);
    }

    @Override
    public List<DomainSettlement> findByStatus(SettlementStatus status) {
        return jpa.findByStatus(status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<DomainSettlement> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }
}
