package net.tanguydev.fraudservice.Infrastructure.Adapters;

import net.tanguydev.fraudservice.Domain.Entities.DomainCustomerRiskProfile;
import net.tanguydev.fraudservice.Domain.Ports.CustomerRiskProfileRepositoryInterface;
import net.tanguydev.fraudservice.Infrastructure.Mappers.FraudMapper;
import net.tanguydev.fraudservice.Infrastructure.Repositories.CustomerRiskProfileJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CustomerRiskProfileRepository implements CustomerRiskProfileRepositoryInterface {

    private final CustomerRiskProfileJpaRepository jpa;
    private final FraudMapper mapper;

    public CustomerRiskProfileRepository(CustomerRiskProfileJpaRepository jpa, FraudMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public DomainCustomerRiskProfile save(DomainCustomerRiskProfile profile) {
        return mapper.toProfileDomain(jpa.save(mapper.toProfileJpa(profile)));
    }

    @Override
    public Optional<DomainCustomerRiskProfile> findByCustomerId(UUID customerId) {
        return jpa.findByCustomerId(customerId).map(mapper::toProfileDomain);
    }
}
