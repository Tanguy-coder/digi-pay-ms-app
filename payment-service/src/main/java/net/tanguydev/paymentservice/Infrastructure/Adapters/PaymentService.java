package net.tanguydev.paymentservice.Infrastructure.Adapters;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Gateways.PaymentRepositoryInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentServiceInterface;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService implements PaymentServiceInterface {

    private final PaymentRepositoryInterface paymentRepository;

    public PaymentService(PaymentRepositoryInterface paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public DomainPayment save(DomainPayment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<DomainPayment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Optional<DomainPayment> findByIdempotencyKey(String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public List<DomainPayment> findBySenderWalletId(UUID senderWalletId) {
        return paymentRepository.findBySenderWalletId(senderWalletId);
    }

    @Override
    public List<DomainPayment> findByReceiverWalletId(UUID receiverWalletId) {
        return paymentRepository.findByReceiverWalletId(receiverWalletId);
    }
}
