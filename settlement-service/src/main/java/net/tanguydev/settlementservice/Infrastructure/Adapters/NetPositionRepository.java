package net.tanguydev.settlementservice.Infrastructure.Adapters;

import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;
import net.tanguydev.settlementservice.Domain.Ports.NetPositionRepositoryInterface;
import net.tanguydev.settlementservice.Infrastructure.Mappers.SettlementMapper;
import net.tanguydev.settlementservice.Infrastructure.Models.NetPosition;
import net.tanguydev.settlementservice.Infrastructure.Repositories.NetPositionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class NetPositionRepository implements NetPositionRepositoryInterface {

    private final NetPositionJpaRepository jpaRepository;
    private final SettlementMapper mapper;

    public NetPositionRepository(NetPositionJpaRepository jpaRepository, SettlementMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainNetPosition save(DomainNetPosition position) {
        NetPosition entity = mapper.toPositionJpa(position);
        NetPosition saved = jpaRepository.save(entity);
        return mapper.toPositionDomain(saved);
    }

    @Override
    public List<DomainNetPosition> saveAll(List<DomainNetPosition> positions) {
        List<NetPosition> entities = positions.stream()
                .map(mapper::toPositionJpa)
                .toList();
        List<NetPosition> saved = jpaRepository.saveAll(entities);
        return saved.stream()
                .map(mapper::toPositionDomain)
                .toList();
    }

    @Override
    public List<DomainNetPosition> findByBatchId(UUID batchId) {
        return jpaRepository.findByBatchId(batchId).stream()
                .map(mapper::toPositionDomain)
                .toList();
    }
}
