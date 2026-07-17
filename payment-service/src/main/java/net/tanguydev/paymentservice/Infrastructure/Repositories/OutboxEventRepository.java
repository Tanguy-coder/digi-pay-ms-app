package net.tanguydev.paymentservice.Infrastructure.Repositories;

import net.tanguydev.paymentservice.Domain.Entities.DomainOutboxEvent;
import net.tanguydev.paymentservice.Domain.Gateways.OutboxEventRepositoryInterface;
import net.tanguydev.paymentservice.Infrastructure.Mappers.PaymentMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class OutboxEventRepository implements OutboxEventRepositoryInterface {

    private final OutboxEventJpaRepository jpaRepository;
    private final PaymentMapper mapper;

    public OutboxEventRepository(OutboxEventJpaRepository jpaRepository, PaymentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainOutboxEvent save(DomainOutboxEvent event) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(event)));
    }

    @Override
    public List<DomainOutboxEvent> findUnpublished() {
        return jpaRepository.findByPublishedFalseOrderByCreatedAtAsc()
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public void markPublished(UUID id) {
        jpaRepository.findById(id).ifPresent(e -> {
            e.setPublished(true);
            e.setPublishedAt(OffsetDateTime.now());
            jpaRepository.save(e);
        });
    }
}
