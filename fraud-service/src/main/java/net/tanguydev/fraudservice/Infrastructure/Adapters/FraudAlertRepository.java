package net.tanguydev.fraudservice.Infrastructure.Adapters;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAlert;
import net.tanguydev.fraudservice.Domain.Ports.FraudAlertRepositoryInterface;
import net.tanguydev.fraudservice.Infrastructure.Mappers.FraudMapper;
import net.tanguydev.fraudservice.Infrastructure.Repositories.FraudAlertJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class FraudAlertRepository implements FraudAlertRepositoryInterface {

    private final FraudAlertJpaRepository jpa;
    private final FraudMapper mapper;

    public FraudAlertRepository(FraudAlertJpaRepository jpa, FraudMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public DomainFraudAlert save(DomainFraudAlert alert) {
        return mapper.toAlertDomain(jpa.save(mapper.toAlertJpa(alert)));
    }

    @Override
    public List<DomainFraudAlert> findByFraudAnalysisId(UUID fraudAnalysisId) {
        return mapper.toAlertDomainList(jpa.findByFraudAnalysisId(fraudAnalysisId));
    }
}
