package net.tanguydev.settlementservice.Infrastructure.Repositories;

import net.tanguydev.settlementservice.Infrastructure.Models.NetPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NetPositionJpaRepository extends JpaRepository<NetPosition, UUID> {

    List<NetPosition> findByBatchId(UUID batchId);
}
