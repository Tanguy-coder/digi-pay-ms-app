package net.tanguydev.settlementservice.Infrastructure.EventStore;

import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class BatchEventStore implements BatchEventStoreInterface {

    private final BatchEventJpaRepository jpaRepository;

    public BatchEventStore(BatchEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void append(BatchEventEntry event) {
        BatchEventEntity entity = toEntity(event);
        jpaRepository.save(entity);
    }

    @Override
    public List<BatchEventEntry> loadEvents(UUID batchId) {
        return jpaRepository.findByBatchIdOrderByAggregateVersionAsc(batchId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<BatchEventEntry> loadEventsSince(UUID batchId, Long afterVersion) {
        return jpaRepository.findByBatchIdAndAggregateVersionGreaterThanOrderByAggregateVersionAsc(batchId, afterVersion)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private BatchEventEntity toEntity(BatchEventEntry event) {
        return BatchEventEntity.builder()
                .batchId(event.getBatchId())
                .eventType(event.getEventType())
                .aggregateVersion(event.getAggregateVersion())
                .occurredAt(event.getOccurredAt())
                .reference(event.getReference())
                .cycle(event.getCycle())
                .currency(event.getCurrency())
                .status(event.getStatus())
                .paymentId(event.getPaymentId())
                .paymentReference(event.getPaymentReference())
                .senderWalletId(event.getSenderWalletId())
                .receiverWalletId(event.getReceiverWalletId())
                .amount(event.getAmount())
                .walletId(event.getWalletId())
                .grossDebit(event.getGrossDebit())
                .grossCredit(event.getGrossCredit())
                .netAmount(event.getNetAmount())
                .positionStatus(event.getPositionStatus())
                .reason(event.getReason())
                .build();
    }

    private BatchEventEntry toDomain(BatchEventEntity entity) {
        BatchEventEntry event = new BatchEventEntry();
        event.setId(entity.getId());
        event.setBatchId(entity.getBatchId());
        event.setEventType(entity.getEventType());
        event.setAggregateVersion(entity.getAggregateVersion());
        event.setOccurredAt(entity.getOccurredAt());
        event.setReference(entity.getReference());
        event.setCycle(entity.getCycle());
        event.setCurrency(entity.getCurrency());
        event.setStatus(entity.getStatus());
        event.setPaymentId(entity.getPaymentId());
        event.setPaymentReference(entity.getPaymentReference());
        event.setSenderWalletId(entity.getSenderWalletId());
        event.setReceiverWalletId(entity.getReceiverWalletId());
        event.setAmount(entity.getAmount());
        event.setWalletId(entity.getWalletId());
        event.setGrossDebit(entity.getGrossDebit());
        event.setGrossCredit(entity.getGrossCredit());
        event.setNetAmount(entity.getNetAmount());
        event.setPositionStatus(entity.getPositionStatus());
        event.setReason(entity.getReason());
        return event;
    }
}
