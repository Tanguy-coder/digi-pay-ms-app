package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Aggregates.SettlementBatchAggregate;
import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.NetPositionRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Validations.Exception.BatchNotFoundException;

import java.util.List;
import java.util.UUID;

public class CalculateNetPositionsUseCase implements CalculateNetPositionsUseCaseInterface {

    private final BatchEventStoreInterface eventStore;
    private final SettlementBatchRepositoryInterface batchRepository;
    private final NetPositionRepositoryInterface netPositionRepository;

    public CalculateNetPositionsUseCase(BatchEventStoreInterface eventStore,
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

        aggregate.calculatePositions();

        for (BatchEventEntry event : aggregate.getUncommittedEvents()) {
            eventStore.append(event);
        }
        aggregate.markEventsCommitted();

        List<DomainNetPosition> positions = aggregate.getNetPositionsList().stream()
                .map(data -> {
                    DomainNetPosition pos = new DomainNetPosition();
                    pos.setId(UUID.randomUUID());
                    pos.setBatchId(batchId);
                    pos.setWalletId(data.getWalletId());
                    pos.setGrossDebit(data.getGrossDebit());
                    pos.setGrossCredit(data.getGrossCredit());
                    pos.setNetAmount(data.getNetAmount());
                    pos.setStatus(data.getStatus());
                    return pos;
                })
                .toList();

        netPositionRepository.saveAll(positions);

        batch.setStatus(aggregate.getStatus());
        batchRepository.save(batch);
    }
}
