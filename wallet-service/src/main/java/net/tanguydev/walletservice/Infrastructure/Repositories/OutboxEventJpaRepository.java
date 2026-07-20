package net.tanguydev.walletservice.Infrastructure.Repositories;

import net.tanguydev.walletservice.Infrastructure.Models.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
}
