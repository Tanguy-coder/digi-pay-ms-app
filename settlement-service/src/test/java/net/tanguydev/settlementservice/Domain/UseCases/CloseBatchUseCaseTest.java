package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.BatchEventType;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Validations.Exception.BatchNotFoundException;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseBatchUseCaseTest {

    @Mock
    private BatchEventStoreInterface eventStore;

    @Mock
    private SettlementBatchRepositoryInterface batchRepository;

    @InjectMocks
    private CloseBatchUseCase useCase;

    @Test
    void execute_shouldCloseBatch() {
        UUID batchId = UUID.randomUUID();
        DomainSettlementBatch batch = new DomainSettlementBatch();
        batch.setId(batchId);
        batch.setStatus(BatchStatus.COLLECTING);

        BatchEventEntry openEvent = buildOpenEvent(batchId);
        BatchEventEntry captureEvent = new BatchEventEntry();
        captureEvent.setBatchId(batchId);
        captureEvent.setEventType(BatchEventType.ENTRY_CAPTURED);
        captureEvent.setPaymentId(UUID.randomUUID());
        captureEvent.setPaymentReference("PAY-001");
        captureEvent.setSenderWalletId(UUID.randomUUID());
        captureEvent.setReceiverWalletId(UUID.randomUUID());
        captureEvent.setAmount(new BigDecimal("1000"));
        captureEvent.setCurrency("XAF");
        captureEvent.setAggregateVersion(2L);
        captureEvent.setOccurredAt(OffsetDateTime.now());

        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(eventStore.loadEvents(batchId)).thenReturn(List.of(openEvent, captureEvent));
        when(batchRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(batchId);

        verify(eventStore).append(any(BatchEventEntry.class));
        verify(batchRepository).save(any(DomainSettlementBatch.class));
    }

    @Test
    void execute_shouldThrowWhenBatchNotFound() {
        UUID batchId = UUID.randomUUID();
        when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> useCase.execute(batchId));
    }

    private BatchEventEntry buildOpenEvent(UUID batchId) {
        BatchEventEntry event = new BatchEventEntry();
        event.setBatchId(batchId);
        event.setEventType(BatchEventType.BATCH_OPENED);
        event.setReference("BATCH-XAF-TEST");
        event.setCycle(SettlementCycle.HOURLY);
        event.setCurrency("XAF");
        event.setAggregateVersion(1L);
        event.setOccurredAt(OffsetDateTime.now());
        return event;
    }
}
