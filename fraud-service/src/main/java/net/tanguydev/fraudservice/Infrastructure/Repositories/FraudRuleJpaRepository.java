package net.tanguydev.fraudservice.Infrastructure.Repositories;

import net.tanguydev.fraudservice.Infrastructure.Models.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FraudRuleJpaRepository extends JpaRepository<FraudRule, UUID> {

    List<FraudRule> findByActiveTrue();
}
