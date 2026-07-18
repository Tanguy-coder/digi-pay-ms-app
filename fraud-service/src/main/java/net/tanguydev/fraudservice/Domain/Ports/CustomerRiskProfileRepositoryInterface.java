package net.tanguydev.fraudservice.Domain.Ports;

import net.tanguydev.fraudservice.Domain.Entities.DomainCustomerRiskProfile;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRiskProfileRepositoryInterface {

    DomainCustomerRiskProfile save(DomainCustomerRiskProfile profile);

    Optional<DomainCustomerRiskProfile> findByCustomerId(UUID customerId);
}
