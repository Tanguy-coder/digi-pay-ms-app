package net.tanguydev.fraudservice.Domain.Ports;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudAnalysisRepositoryInterface {

    DomainFraudAnalysis save(DomainFraudAnalysis analysis);

    Optional<DomainFraudAnalysis> findByPaymentId(UUID paymentId);

    List<DomainFraudAnalysis> findByCustomerId(UUID customerId);
}
