package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Aggregates.SettlementBatchAggregate;
import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.NetPositionStatus;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.NetPositionRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Validations.Exception.BatchNotFoundException;

import java.util.List;
import java.util.UUID;

public class ApplySettlementUseCase implements ApplySettlementUseCaseInterface {

    private final BatchEventStoreInterface eventStore;
    private final SettlementBatchRepositoryInterface batchRepository;
    private final NetPositionRepositoryInterface netPositionRepository;

    public ApplySettlementUseCase(BatchEventStoreInterface eventStore,
                                  SettlementBatchRepositoryInterface batchRepository,
                                  NetPositionRepositoryInterface netPositionRepository) {
        this.eventStore = eventStore;
        this.batchRepository = batchRepository;
        this.netPositionRepository = netPositionRepository;
    }

    @Override
    public void execute(UUID batchId) {
        DomainSettlementBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        List<BatchEventEntry> events = eventStore.loadEvents(batchId);
        SettlementBatchAggregate aggregate = SettlementBatchAggregate.reconstitute(events);

        aggregate.applySettlement();

        for (BatchEventEntry event : aggregate.getUncommittedEvents()) {
            eventStore.append(event);
        }
        aggregate.markEventsCommitted();

        List<DomainNetPosition> positions = netPositionRepository.findByBatchId(batchId);
        for (DomainNetPosition position : positions) {
            position.setStatus(NetPositionStatus.SETTLED);
            netPositionRepository.save(position);
        }

        batch.setStatus(aggregate.getStatus());
        batchRepository.save(batch);
    }
}
