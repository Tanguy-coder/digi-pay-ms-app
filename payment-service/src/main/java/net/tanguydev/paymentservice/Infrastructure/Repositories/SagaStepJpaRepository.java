package net.tanguydev.paymentservice.Infrastructure.Repositories;

import net.tanguydev.paymentservice.Infrastructure.Models.SagaStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SagaStepJpaRepository extends JpaRepository<SagaStep, UUID> {

    List<SagaStep> findByPaymentIdOrderByStepOrderAsc(UUID paymentId);
}
