package net.tanguydev.paymentservice.Infrastructure.Repositories;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Gateways.PaymentRepositoryInterface;
import net.tanguydev.paymentservice.Infrastructure.Mappers.PaymentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepository implements PaymentRepositoryInterface {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper mapper;

    public PaymentRepository(PaymentJpaRepository jpaRepository, PaymentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainPayment save(DomainPayment payment) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(payment)));
    }

    @Override
    public Optional<DomainPayment> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<DomainPayment> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public List<DomainPayment> findBySenderWalletId(UUID senderWalletId) {
        return mapper.toDomainList(jpaRepository.findBySenderWalletId(senderWalletId));
    }

    @Override
    public List<DomainPayment> findByReceiverWalletId(UUID receiverWalletId) {
        return mapper.toDomainList(jpaRepository.findByReceiverWalletId(receiverWalletId));
    }
}
