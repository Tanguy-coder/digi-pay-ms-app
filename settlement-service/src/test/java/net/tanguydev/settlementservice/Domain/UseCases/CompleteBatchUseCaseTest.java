package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.*;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEventPublisherInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteBatchUseCaseTest {

    @Mock
    private BatchEventStoreInterface eventStore;

    @Mock
    private SettlementBatchRepositoryInterface batchRepository;

    @Mock
    private SettlementEventPublisherInterface eventPublisher;

    @InjectMocks
    private CompleteBatchUseCase useCase;

    @Test
    void execute_shouldCompleteBatchAndPublishEvent() {
        UUID batchId = UUID.randomUUID();
        UUID walletA = UUID.randomUUID();
        UUID walletB = UUID.randomUUID();

        DomainSettlementBatch batch = new DomainSettlementBatch();
        batch.setId(batchId);
        batch.setStatus(BatchStatus.SETTLING);

        List<BatchEventEntry> events = buildEventsUpToSettlementApplied(batchId, walletA, walletB);

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(eventStore.loadEvents(batchId)).thenReturn(events);
        when(batchRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(batchId);

        verify(eventStore).append(any(BatchEventEntry.class));
        verify(batchRepository).save(any(DomainSettlementBatch.class));
        verify(eventPublisher).publishBatchCompleted(any(DomainSettlementBatch.class));
    }

    private List<BatchEventEntry> buildEventsUpToSettlementApplied(UUID batchId, UUID walletA, UUID walletB) {
        BatchEventEntry open = new BatchEventEntry();
        open.setBatchId(batchId);
        open.setEventType(BatchEventType.BATCH_OPENED);
        open.setReference("BATCH-XAF-TEST");
        open.setCycle(SettlementCycle.HOURLY);
        open.setCurrency("XAF");
        open.setAggregateVersion(1L);
        open.setOccurredAt(OffsetDateTime.now());

        BatchEventEntry capture = new BatchEventEntry();
        capture.setBatchId(batchId);
        capture.setEventType(BatchEventType.ENTRY_CAPTURED);
        capture.setPaymentId(UUID.randomUUID());
        capture.setPaymentReference("PAY-001");
        capture.setSenderWalletId(walletA);
        capture.setReceiverWalletId(walletB);
        capture.setAmount(new BigDecimal("5000"));
        capture.setCurrency("XAF");
        capture.setAggregateVersion(2L);
        capture.setOccurredAt(OffsetDateTime.now());

        BatchEventEntry close = new BatchEventEntry();
        close.setBatchId(batchId);
        close.setEventType(BatchEventType.BATCH_CLOSED);
        close.setAggregateVersion(3L);
        close.setOccurredAt(OffsetDateTime.now());

        BatchEventEntry posCalcA = new BatchEventEntry();
        posCalcA.setBatchId(batchId);
        posCalcA.setEventType(BatchEventType.POSITIONS_CALCULATED);
        posCalcA.setWalletId(walletA);
        posCalcA.setGrossDebit(new BigDecimal("5000"));
        posCalcA.setGrossCredit(BigDecimal.ZERO);
        posCalcA.setNetAmount(new BigDecimal("-5000"));
        posCalcA.setPositionStatus(NetPositionStatus.CALCULATED);
        posCalcA.setAggregateVersion(4L);
        posCalcA.setOccurredAt(OffsetDateTime.now());

        BatchEventEntry posCalcB = new BatchEventEntry();
        posCalcB.setBatchId(batchId);
        posCalcB.setEventType(BatchEventType.POSITIONS_CALCULATED);
        posCalcB.setWalletId(walletB);
        posCalcB.setGrossDebit(BigDecimal.ZERO);
        posCalcB.setGrossCredit(new BigDecimal("5000"));
        posCalcB.setNetAmount(new BigDecimal("5000"));
        posCalcB.setPositionStatus(NetPositionStatus.CALCULATED);
        posCalcB.setAggregateVersion(5L);
        posCalcB.setOccurredAt(OffsetDateTime.now());

        BatchEventEntry applied = new BatchEventEntry();
        applied.setBatchId(batchId);
        applied.setEventType(BatchEventType.SETTLEMENT_APPLIED);
        applied.setAggregateVersion(6L);
        applied.setOccurredAt(OffsetDateTime.now());

        return List.of(open, capture, close, posCalcA, posCalcB, applied);
    }
}
