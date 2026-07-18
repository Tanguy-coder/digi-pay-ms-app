package net.tanguydev.settlementservice.Infrastructure.Repositories;

import net.tanguydev.settlementservice.Domain.Enums.SettlementStatus;
import net.tanguydev.settlementservice.Infrastructure.Models.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementJpaRepository extends JpaRepository<Settlement, UUID> {
    Optional<Settlement> findByReference(String reference);
    List<Settlement> findByStatus(SettlementStatus status);
}
