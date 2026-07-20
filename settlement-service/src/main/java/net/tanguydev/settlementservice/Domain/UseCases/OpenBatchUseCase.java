package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Aggregates.SettlementBatchAggregate;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OpenBatchUseCase implements OpenBatchUseCaseInterface {

    private final BatchEventStoreInterface eventStore;
    private final SettlementBatchRepositoryInterface batchRepository;

    public OpenBatchUseCase(BatchEventStoreInterface eventStore,
                            SettlementBatchRepositoryInterface batchRepository) {
        this.eventStore = eventStore;
        this.batchRepository = batchRepository;
    }

    @Override
    public DomainSettlementBatch execute(SettlementCycle cycle, String currency) {
        UUID batchId = UUID.randomUUID();
        String reference = "BATCH-" + currency + "-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        SettlementBatchAggregate aggregate = new SettlementBatchAggregate();
        aggregate.openBatch(batchId, reference, cycle, currency);

        for (BatchEventEntry event : aggregate.getUncommittedEvents()) {
            eventStore.append(event);
        }
        aggregate.markEventsCommitted();

        DomainSettlementBatch projection = toProjection(aggregate);
        return batchRepository.save(projection);
    }

    private DomainSettlementBatch toProjection(SettlementBatchAggregate aggregate) {
        DomainSettlementBatch batch = new DomainSettlementBatch();
        batch.setId(aggregate.getBatchId());
        batch.setReference(aggregate.getReference());
        batch.setStatus(aggregate.getStatus());
        batch.setCycle(aggregate.getCycle());
        batch.setCurrency(aggregate.getCurrency());
        batch.setTotalEntries(aggregate.getTotalEntries());
        batch.setTotalAmount(aggregate.getTotalAmount());
        batch.setOpenedAt(aggregate.getOpenedAt());
        batch.setClosedAt(aggregate.getClosedAt());
        batch.setSettledAt(aggregate.getSettledAt());
        batch.setCreatedAt(aggregate.getOpenedAt());
        return batch;
    }
}
