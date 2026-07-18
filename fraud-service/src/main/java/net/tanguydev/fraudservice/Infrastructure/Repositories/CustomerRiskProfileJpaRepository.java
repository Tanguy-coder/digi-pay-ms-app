package net.tanguydev.fraudservice.Infrastructure.Repositories;

import net.tanguydev.fraudservice.Infrastructure.Models.CustomerRiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRiskProfileJpaRepository extends JpaRepository<CustomerRiskProfile, UUID> {

    Optional<CustomerRiskProfile> findByCustomerId(UUID customerId);
}
