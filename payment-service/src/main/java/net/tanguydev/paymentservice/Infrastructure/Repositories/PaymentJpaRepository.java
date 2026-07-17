package net.tanguydev.paymentservice.Infrastructure.Repositories;

import net.tanguydev.paymentservice.Infrastructure.Models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findBySenderWalletId(UUID senderWalletId);

    List<Payment> findByReceiverWalletId(UUID receiverWalletId);
}
