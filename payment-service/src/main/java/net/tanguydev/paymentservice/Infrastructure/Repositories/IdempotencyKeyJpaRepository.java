package net.tanguydev.paymentservice.Infrastructure.Repositories;

import net.tanguydev.paymentservice.Infrastructure.Models.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKey, String> {
}
