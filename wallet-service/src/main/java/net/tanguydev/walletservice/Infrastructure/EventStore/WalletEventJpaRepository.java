package net.tanguydev.walletservice.Infrastructure.EventStore;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletEventJpaRepository extends JpaRepository<WalletEventEntity, UUID> {

    List<WalletEventEntity> findByWalletIdOrderByAggregateVersionAsc(UUID walletId);

    List<WalletEventEntity> findByWalletIdAndAggregateVersionGreaterThanOrderByAggregateVersionAsc(
            UUID walletId, Long afterVersion);
}
