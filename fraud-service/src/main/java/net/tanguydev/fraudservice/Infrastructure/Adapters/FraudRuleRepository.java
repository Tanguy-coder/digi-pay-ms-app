package net.tanguydev.fraudservice.Infrastructure.Adapters;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudRule;
import net.tanguydev.fraudservice.Domain.Ports.FraudRuleRepositoryInterface;
import net.tanguydev.fraudservice.Infrastructure.Mappers.FraudMapper;
import net.tanguydev.fraudservice.Infrastructure.Repositories.FraudRuleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FraudRuleRepository implements FraudRuleRepositoryInterface {

    private final FraudRuleJpaRepository jpa;
    private final FraudMapper mapper;

    public FraudRuleRepository(FraudRuleJpaRepository jpa, FraudMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<DomainFraudRule> findAllActive() {
        return mapper.toRuleDomainList(jpa.findByActiveTrue());
    }
}
