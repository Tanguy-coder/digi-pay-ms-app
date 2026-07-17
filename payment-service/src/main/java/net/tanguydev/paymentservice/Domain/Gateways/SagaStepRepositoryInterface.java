package net.tanguydev.paymentservice.Domain.Gateways;

import net.tanguydev.paymentservice.Domain.Entities.DomainSagaStep;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaStepRepositoryInterface {

    DomainSagaStep save(DomainSagaStep step);

    List<DomainSagaStep> findByPaymentId(UUID paymentId);

    Optional<DomainSagaStep> findById(UUID id);
}
