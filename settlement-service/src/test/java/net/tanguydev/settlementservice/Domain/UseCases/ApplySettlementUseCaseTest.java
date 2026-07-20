package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.*;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.NetPositionRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplySettlementUseCaseTest {

    @Mock
    private BatchEventStoreInterface eventStore;

    @Mock
    private SettlementBatchRepositoryInterface batchRepository;

    @Mock
    private NetPositionRepositoryInterface netPositionRepository;

    @InjectMocks
    private ApplySettlementUseCase useCase;

    @Test
    void execute_shouldApplySettlementAndUpdatePositions() {
        UUID batchId = UUID.randomUUID();
        UUID walletA = UUID.randomUUID();
        UUID walletB = UUID.randomUUID();

        DomainSettlementBatch batch = new DomainSettlementBatch();
        batch.setId(batchId);
        batch.setStatus(BatchStatus.SETTLING);

        DomainNetPosition posA = new DomainNetPosition();
        posA.setId(UUID.randomUUID());
        posA.setBatchId(batchId);
        posA.setWalletId(walletA);
        posA.setStatus(NetPositionStatus.CALCULATED);

        DomainNetPosition posB = new DomainNetPosition();
        posB.setId(UUID.randomUUID());
        posB.setBatchId(batchId);
        posB.setWalletId(walletB);
        posB.setStatus(NetPositionStatus.CALCULATED);

        List<BatchEventEntry> events = buildEventsUpToSettling(batchId, walletA, walletB);

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(eventStore.loadEvents(batchId)).thenReturn(events);
        when(netPositionRepository.findByBatchId(batchId)).thenReturn(List.of(posA, posB));
        when(netPositionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(batchRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(batchId);

        verify(netPositionRepository, times(2)).save(any(DomainNetPosition.class));
        verify(eventStore).append(any(BatchEventEntry.class));
    }

    private List<BatchEventEntry> buildEventsUpToSettling(UUID batchId, UUID walletA, UUID walletB) {
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

        return List.of(open, capture, close, posCalcA, posCalcB);
    }
}
