package net.tanguydev.fraudservice.Infrastructure.Adapters;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Ports.FraudAnalysisRepositoryInterface;
import net.tanguydev.fraudservice.Infrastructure.Mappers.FraudMapper;
import net.tanguydev.fraudservice.Infrastructure.Repositories.FraudAnalysisJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FraudAnalysisRepository implements FraudAnalysisRepositoryInterface {

    private final FraudAnalysisJpaRepository jpa;
    private final FraudMapper mapper;

    public FraudAnalysisRepository(FraudAnalysisJpaRepository jpa, FraudMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public DomainFraudAnalysis save(DomainFraudAnalysis analysis) {
        return mapper.toAnalysisDomain(jpa.save(mapper.toAnalysisJpa(analysis)));
    }

    @Override
    public Optional<DomainFraudAnalysis> findByPaymentId(UUID paymentId) {
        return jpa.findByPaymentId(paymentId).map(mapper::toAnalysisDomain);
    }

    @Override
    public List<DomainFraudAnalysis> findByCustomerId(UUID customerId) {
        return mapper.toAnalysisDomainList(jpa.findByCustomerId(customerId));
    }
}
