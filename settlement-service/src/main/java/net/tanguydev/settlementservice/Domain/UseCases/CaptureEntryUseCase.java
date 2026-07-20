package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Aggregates.SettlementBatchAggregate;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Validations.Exception.NoOpenBatchException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class CaptureEntryUseCase implements CaptureEntryUseCaseInterface {

    private final BatchEventStoreInterface eventStore;
    private final SettlementBatchRepositoryInterface batchRepository;
    private final SettlementEntryRepositoryInterface entryRepository;

    public CaptureEntryUseCase(BatchEventStoreInterface eventStore,
                               SettlementBatchRepositoryInterface batchRepository,
                               SettlementEntryRepositoryInterface entryRepository) {
        this.eventStore = eventStore;
        this.batchRepository = batchRepository;
        this.entryRepository = entryRepository;
    }

    @Override
    public void execute(CaptureEntryCommand command) {
        if (entryRepository.existsByPaymentId(command.getPaymentId())) {
            return;
        }

        DomainSettlementBatch currentBatch = batchRepository.findCurrentOpenBatch(command.getCurrency())
                .orElseThrow(() -> new NoOpenBatchException(command.getCurrency()));

        List<BatchEventEntry> events = eventStore.loadEvents(currentBatch.getId());
        SettlementBatchAggregate aggregate = SettlementBatchAggregate.reconstitute(events);

        aggregate.captureEntry(
                command.getPaymentId(),
                command.getPaymentReference(),
                command.getSenderWalletId(),
                command.getReceiverWalletId(),
                command.getAmount(),
                command.getCurrency()
        );

        for (BatchEventEntry event : aggregate.getUncommittedEvents()) {
            eventStore.append(event);
        }
        aggregate.markEventsCommitted();

        DomainSettlementEntry entry = new DomainSettlementEntry();
        entry.setId(UUID.randomUUID());
        entry.setBatchId(currentBatch.getId());
        entry.setPaymentId(command.getPaymentId());
        entry.setPaymentReference(command.getPaymentReference());
        entry.setSenderWalletId(command.getSenderWalletId());
        entry.setReceiverWalletId(command.getReceiverWalletId());
        entry.setAmount(command.getAmount());
        entry.setCurrency(command.getCurrency());
        entry.setCapturedAt(OffsetDateTime.now());
        entryRepository.save(entry);

        DomainSettlementBatch updatedBatch = toProjection(aggregate, currentBatch);
        batchRepository.save(updatedBatch);
    }

    private DomainSettlementBatch toProjection(SettlementBatchAggregate aggregate, DomainSettlementBatch existing) {
        existing.setStatus(aggregate.getStatus());
        existing.setTotalEntries(aggregate.getTotalEntries());
        existing.setTotalAmount(aggregate.getTotalAmount());
        return existing;
    }
}
