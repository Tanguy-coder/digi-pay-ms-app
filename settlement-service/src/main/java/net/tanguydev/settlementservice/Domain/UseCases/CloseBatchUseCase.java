package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Aggregates.SettlementBatchAggregate;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Validations.Exception.BatchNotFoundException;

import java.util.List;
import java.util.UUID;

public class CloseBatchUseCase implements CloseBatchUseCaseInterface {

    private final BatchEventStoreInterface eventStore;
    private final SettlementBatchRepositoryInterface batchRepository;

    public CloseBatchUseCase(BatchEventStoreInterface eventStore,
                             SettlementBatchRepositoryInterface batchRepository) {
        this.eventStore = eventStore;
        this.batchRepository = batchRepository;
    }

    @Override
    public void execute(UUID batchId) {
        DomainSettlementBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        List<BatchEventEntry> events = eventStore.loadEvents(batchId);
        SettlementBatchAggregate aggregate = SettlementBatchAggregate.reconstitute(events);

        aggregate.closeBatch();

        for (BatchEventEntry event : aggregate.getUncommittedEvents()) {
            eventStore.append(event);
        }
        aggregate.markEventsCommitted();

        batch.setStatus(aggregate.getStatus());
        batch.setClosedAt(aggregate.getClosedAt());
        batchRepository.save(batch);
    }
}
