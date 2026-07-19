package net.tanguydev.walletservice.Infrastructure.EventStore;

import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Ports.EventStoreInterface;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class WalletEventStore implements EventStoreInterface {

    private final WalletEventJpaRepository jpaRepository;

    public WalletEventStore(WalletEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void append(WalletEventEntry event) {
        WalletEventEntity entity = toEntity(event);
        jpaRepository.save(entity);
    }

    @Override
    public List<WalletEventEntry> loadEvents(UUID walletId) {
        return jpaRepository.findByWalletIdOrderByAggregateVersionAsc(walletId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<WalletEventEntry> loadEventsSince(UUID walletId, Long afterVersion) {
        return jpaRepository
                .findByWalletIdAndAggregateVersionGreaterThanOrderByAggregateVersionAsc(walletId, afterVersion)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private WalletEventEntity toEntity(WalletEventEntry entry) {
        return WalletEventEntity.builder()
                .walletId(entry.getWalletId())
                .eventType(entry.getEventType())
                .customerId(entry.getCustomerId())
                .currency(entry.getCurrency())
                .walletType(entry.getWalletType())
                .walletNumber(entry.getWalletNumber())
                .amount(entry.getAmount())
                .dailyLimit(entry.getDailyLimit())
                .monthlyLimit(entry.getMonthlyLimit())
                .status(entry.getStatus())
                .aggregateVersion(entry.getAggregateVersion())
                .occurredAt(entry.getOccurredAt())
                .build();
    }

    private WalletEventEntry toDomain(WalletEventEntity entity) {
        WalletEventEntry entry = new WalletEventEntry();
        entry.setId(entity.getId());
        entry.setWalletId(entity.getWalletId());
        entry.setEventType(entity.getEventType());
        entry.setCustomerId(entity.getCustomerId());
        entry.setCurrency(entity.getCurrency());
        entry.setWalletType(entity.getWalletType());
        entry.setWalletNumber(entity.getWalletNumber());
        entry.setAmount(entity.getAmount());
        entry.setDailyLimit(entity.getDailyLimit());
        entry.setMonthlyLimit(entity.getMonthlyLimit());
        entry.setStatus(entity.getStatus());
        entry.setAggregateVersion(entity.getAggregateVersion());
        entry.setOccurredAt(entity.getOccurredAt());
        return entry;
    }
}
