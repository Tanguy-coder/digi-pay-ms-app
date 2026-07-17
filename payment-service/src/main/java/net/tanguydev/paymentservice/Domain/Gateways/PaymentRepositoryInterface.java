package net.tanguydev.paymentservice.Domain.Gateways;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryInterface {

    DomainPayment save(DomainPayment payment);

    Optional<DomainPayment> findById(UUID id);

    Optional<DomainPayment> findByIdempotencyKey(String idempotencyKey);

    List<DomainPayment> findBySenderWalletId(UUID senderWalletId);

    List<DomainPayment> findByReceiverWalletId(UUID receiverWalletId);
}
