package net.tanguydev.paymentservice.Infrastructure.Repositories;

import net.tanguydev.paymentservice.Domain.Entities.DomainSagaStep;
import net.tanguydev.paymentservice.Domain.Gateways.SagaStepRepositoryInterface;
import net.tanguydev.paymentservice.Infrastructure.Mappers.PaymentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SagaStepRepository implements SagaStepRepositoryInterface {

    private final SagaStepJpaRepository jpaRepository;
    private final PaymentMapper mapper;

    public SagaStepRepository(SagaStepJpaRepository jpaRepository, PaymentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainSagaStep save(DomainSagaStep step) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(step)));
    }

    @Override
    public List<DomainSagaStep> findByPaymentId(UUID paymentId) {
        return jpaRepository.findByPaymentIdOrderByStepOrderAsc(paymentId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<DomainSagaStep> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
