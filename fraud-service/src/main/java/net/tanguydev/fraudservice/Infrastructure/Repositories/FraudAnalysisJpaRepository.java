package net.tanguydev.fraudservice.Infrastructure.Repositories;

import net.tanguydev.fraudservice.Infrastructure.Models.FraudAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudAnalysisJpaRepository extends JpaRepository<FraudAnalysis, UUID> {

    Optional<FraudAnalysis> findByPaymentId(UUID paymentId);

    List<FraudAnalysis> findByCustomerId(UUID customerId);
}
