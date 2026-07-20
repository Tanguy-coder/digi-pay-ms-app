package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Ports.BatchEventStoreInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenBatchUseCaseTest {

    @Mock
    private BatchEventStoreInterface eventStore;

    @Mock
    private SettlementBatchRepositoryInterface batchRepository;

    @InjectMocks
    private OpenBatchUseCase useCase;

    @Test
    void execute_shouldOpenNewBatch() {
        when(batchRepository.save(any(DomainSettlementBatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DomainSettlementBatch result = useCase.execute(SettlementCycle.HOURLY, "XAF");

        assertNotNull(result);
        assertEquals(BatchStatus.OPEN, result.getStatus());
        assertEquals(SettlementCycle.HOURLY, result.getCycle());
        assertEquals("XAF", result.getCurrency());
        assertTrue(result.getReference().startsWith("BATCH-XAF-"));

        ArgumentCaptor<BatchEventEntry> eventCaptor = ArgumentCaptor.forClass(BatchEventEntry.class);
        verify(eventStore).append(eventCaptor.capture());
        assertEquals(net.tanguydev.settlementservice.Domain.Enums.BatchEventType.BATCH_OPENED, eventCaptor.getValue().getEventType());
        verify(batchRepository).save(any(DomainSettlementBatch.class));
    }
}
