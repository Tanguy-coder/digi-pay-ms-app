package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.BatchEventType;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.NetPositionRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateNetPositionsUseCaseTest {

    @Mock
    private BatchEventStoreInterface eventStore;

    @Mock
    private SettlementBatchRepositoryInterface batchRepository;

    @Mock
    private NetPositionRepositoryInterface netPositionRepository;

    @InjectMocks
    private CalculateNetPositionsUseCase useCase;

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldCalculateNetPositionsForMultipleWallets() {
        UUID batchId = UUID.randomUUID();
        UUID walletA = UUID.randomUUID();
        UUID walletB = UUID.randomUUID();

        DomainSettlementBatch batch = new DomainSettlementBatch();
        batch.setId(batchId);
        batch.setStatus(BatchStatus.CALCULATING);

        BatchEventEntry openEvent = new BatchEventEntry();
        openEvent.setBatchId(batchId);
        openEvent.setEventType(BatchEventType.BATCH_OPENED);
        openEvent.setReference("BATCH-XAF-TEST");
        openEvent.setCycle(SettlementCycle.HOURLY);
        openEvent.setCurrency("XAF");
        openEvent.setAggregateVersion(1L);
        openEvent.setOccurredAt(OffsetDateTime.now());

        BatchEventEntry captureEvent = new BatchEventEntry();
        captureEvent.setBatchId(batchId);
        captureEvent.setEventType(BatchEventType.ENTRY_CAPTURED);
        captureEvent.setPaymentId(UUID.randomUUID());
        captureEvent.setPaymentReference("PAY-001");
        captureEvent.setSenderWalletId(walletA);
        captureEvent.setReceiverWalletId(walletB);
        captureEvent.setAmount(new BigDecimal("10000"));
        captureEvent.setCurrency("XAF");
        captureEvent.setAggregateVersion(2L);
        captureEvent.setOccurredAt(OffsetDateTime.now());

        BatchEventEntry closeEvent = new BatchEventEntry();
        closeEvent.setBatchId(batchId);
        closeEvent.setEventType(BatchEventType.BATCH_CLOSED);
        closeEvent.setAggregateVersion(3L);
        closeEvent.setOccurredAt(OffsetDateTime.now());

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(eventStore.loadEvents(batchId)).thenReturn(List.of(openEvent, captureEvent, closeEvent));
        when(netPositionRepository.saveAll(any(List.class))).thenAnswer(i -> i.getArgument(0));
        when(batchRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(batchId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainNetPosition>> positionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(netPositionRepository).saveAll(positionsCaptor.capture());

        List<DomainNetPosition> positions = positionsCaptor.getValue();
        assertEquals(2, positions.size());

        verify(eventStore, atLeast(2)).append(any(BatchEventEntry.class));
    }
}
