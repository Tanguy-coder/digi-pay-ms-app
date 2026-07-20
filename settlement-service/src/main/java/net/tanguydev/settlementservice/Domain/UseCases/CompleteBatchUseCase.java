package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Aggregates.SettlementBatchAggregate;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEventPublisherInterface;
import net.tanguydev.settlementservice.Domain.Validations.Exception.BatchNotFoundException;

import java.util.List;
import java.util.UUID;

public class CompleteBatchUseCase implements CompleteBatchUseCaseInterface {

    private final BatchEventStoreInterface eventStore;
    private final SettlementBatchRepositoryInterface batchRepository;
    private final SettlementEventPublisherInterface eventPublisher;

    public CompleteBatchUseCase(BatchEventStoreInterface eventStore,
                                SettlementBatchRepositoryInterface batchRepository,
                                SettlementEventPublisherInterface eventPublisher) {
        this.eventStore = eventStore;
        this.batchRepository = batchRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute(UUID batchId) {
        DomainSettlementBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        List<BatchEventEntry> events = eventStore.loadEvents(batchId);
        SettlementBatchAggregate aggregate = SettlementBatchAggregate.reconstitute(events);

        aggregate.completeBatch();

        for (BatchEventEntry event : aggregate.getUncommittedEvents()) {
            eventStore.append(event);
        }
        aggregate.markEventsCommitted();

        batch.setStatus(aggregate.getStatus());
        batch.setSettledAt(aggregate.getSettledAt());
        batchRepository.save(batch);

        eventPublisher.publishBatchCompleted(batch);
    }
}
