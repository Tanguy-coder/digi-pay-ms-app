package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Enums.BatchEventType;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Validations.Exception.NoOpenBatchException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptureEntryUseCaseTest {

    @Mock
    private BatchEventStoreInterface eventStore;

    @Mock
    private SettlementBatchRepositoryInterface batchRepository;

    @Mock
    private SettlementEntryRepositoryInterface entryRepository;

    @InjectMocks
    private CaptureEntryUseCase useCase;

    @Test
    void execute_shouldCaptureEntry() {
        UUID batchId = UUID.randomUUID();
        CaptureEntryCommand command = buildCommand();

        DomainSettlementBatch batch = new DomainSettlementBatch();
        batch.setId(batchId);
        batch.setStatus(BatchStatus.OPEN);

        BatchEventEntry openEvent = new BatchEventEntry();
        openEvent.setBatchId(batchId);
        openEvent.setEventType(BatchEventType.BATCH_OPENED);
        openEvent.setReference("BATCH-XAF-TEST");
        openEvent.setCycle(SettlementCycle.HOURLY);
        openEvent.setCurrency("XAF");
        openEvent.setAggregateVersion(1L);
        openEvent.setOccurredAt(OffsetDateTime.now());

        when(entryRepository.existsByPaymentId(command.getPaymentId())).thenReturn(false);
        when(batchRepository.findCurrentOpenBatch("XAF")).thenReturn(Optional.of(batch));
        when(eventStore.loadEvents(batchId)).thenReturn(List.of(openEvent));
        when(batchRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(entryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(command);

        verify(eventStore).append(any(BatchEventEntry.class));
        verify(entryRepository).save(any(DomainSettlementEntry.class));
        verify(batchRepository).save(any(DomainSettlementBatch.class));
    }

    @Test
    void execute_shouldSkipDuplicatePayment() {
        CaptureEntryCommand command = buildCommand();
        when(entryRepository.existsByPaymentId(command.getPaymentId())).thenReturn(true);

        useCase.execute(command);

        verify(eventStore, never()).append(any());
        verify(entryRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenNoOpenBatch() {
        CaptureEntryCommand command = buildCommand();
        when(entryRepository.existsByPaymentId(command.getPaymentId())).thenReturn(false);
        when(batchRepository.findCurrentOpenBatch("XAF")).thenReturn(Optional.empty());

        assertThrows(NoOpenBatchException.class, () -> useCase.execute(command));
    }

    private CaptureEntryCommand buildCommand() {
        CaptureEntryCommand command = new CaptureEntryCommand();
        command.setPaymentId(UUID.randomUUID());
        command.setPaymentReference("PAY-001");
        command.setSenderWalletId(UUID.randomUUID());
        command.setReceiverWalletId(UUID.randomUUID());
        command.setAmount(new BigDecimal("5000"));
        command.setFeeAmount(BigDecimal.ZERO);
        command.setCurrency("XAF");
        return command;
    }
}
