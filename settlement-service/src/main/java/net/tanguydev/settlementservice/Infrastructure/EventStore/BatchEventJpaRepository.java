package net.tanguydev.settlementservice.Infrastructure.EventStore;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BatchEventJpaRepository extends JpaRepository<BatchEventEntity, UUID> {

    List<BatchEventEntity> findByBatchIdOrderByAggregateVersionAsc(UUID batchId);

    List<BatchEventEntity> findByBatchIdAndAggregateVersionGreaterThanOrderByAggregateVersionAsc(UUID batchId, Long afterVersion);
}
