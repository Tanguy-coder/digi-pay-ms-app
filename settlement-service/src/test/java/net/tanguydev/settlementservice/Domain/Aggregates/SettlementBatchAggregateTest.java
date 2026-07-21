package net.tanguydev.settlementservice.Domain.Aggregates;

import net.tanguydev.settlementservice.Domain.Enums.BatchEventType;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.NetPositionStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Validations.Exception.DuplicateEntryException;
import net.tanguydev.settlementservice.Domain.Validations.Exception.InvalidBatchStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SettlementBatchAggregateTest {

    private SettlementBatchAggregate aggregate;
    private UUID batchId;

    @BeforeEach
    void setUp() {
        aggregate = new SettlementBatchAggregate();
        batchId = UUID.randomUUID();
        aggregate.openBatch(batchId, "BATCH-XAF-20260719-100000", SettlementCycle.HOURLY, "XAF");
        aggregate.markEventsCommitted();
    }

    @Test
    void openBatch_shouldInitializeState() {
        SettlementBatchAggregate fresh = new SettlementBatchAggregate();
        UUID id = UUID.randomUUID();
        fresh.openBatch(id, "BATCH-XAF-TEST", SettlementCycle.DAILY, "XAF");

        assertEquals(id, fresh.getBatchId());
        assertEquals("BATCH-XAF-TEST", fresh.getReference());
        assertEquals(BatchStatus.OPEN, fresh.getStatus());
        assertEquals(SettlementCycle.DAILY, fresh.getCycle());
        assertEquals("XAF", fresh.getCurrency());
        assertEquals(0, fresh.getTotalEntries());
        assertEquals(BigDecimal.ZERO, fresh.getTotalAmount());
        assertNotNull(fresh.getOpenedAt());
        assertEquals(1, fresh.getUncommittedEvents().size());
        assertEquals(BatchEventType.BATCH_OPENED, fresh.getUncommittedEvents().getFirst().getEventType());
    }

    @Test
    void captureEntry_shouldAddEntryAndUpdateTotals() {
        UUID paymentId = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();

        aggregate.captureEntry(paymentId, "PAY-001", sender, receiver, new BigDecimal("5000"), "XAF");

        assertEquals(1, aggregate.getTotalEntries());
        assertEquals(new BigDecimal("5000"), aggregate.getTotalAmount());
        assertEquals(BatchStatus.COLLECTING, aggregate.getStatus());
    }

    @Test
    void captureEntry_shouldTransitionToCollecting() {
        assertEquals(BatchStatus.OPEN, aggregate.getStatus());

        aggregate.captureEntry(UUID.randomUUID(), "PAY-001", UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000"), "XAF");

        assertEquals(BatchStatus.COLLECTING, aggregate.getStatus());
    }

    @Test
    void captureEntry_shouldRejectDuplicatePayment() {
        UUID paymentId = UUID.randomUUID();
        aggregate.captureEntry(paymentId, "PAY-001", UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000"), "XAF");

        assertThrows(DuplicateEntryException.class, () ->
                aggregate.captureEntry(paymentId, "PAY-001", UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000"), "XAF"));
    }

    @Test
    void captureEntry_shouldRejectWhenBatchClosed() {
        aggregate.captureEntry(UUID.randomUUID(), "PAY-001", UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000"), "XAF");
        aggregate.closeBatch();

        assertThrows(InvalidBatchStatusException.class, () ->
                aggregate.captureEntry(UUID.randomUUID(), "PAY-002", UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("2000"), "XAF"));
    }

    @Test
    void closeBatch_shouldTransitionToCalculating() {
        aggregate.captureEntry(UUID.randomUUID(), "PAY-001", UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000"), "XAF");
        aggregate.closeBatch();

        assertEquals(BatchStatus.CALCULATING, aggregate.getStatus());
        assertNotNull(aggregate.getClosedAt());
    }

    @Test
    void closeBatch_shouldRejectWhenAlreadyClosed() {
        aggregate.closeBatch();

        assertThrows(InvalidBatchStatusException.class, () -> aggregate.closeBatch());
    }

    @Test
    void calculatePositions_shouldComputeNetPositions() {
        UUID walletA = UUID.randomUUID();
        UUID walletB = UUID.randomUUID();
        UUID walletC = UUID.randomUUID();

        // A sends 5000 to B
        aggregate.captureEntry(UUID.randomUUID(), "PAY-001", walletA, walletB, new BigDecimal("5000"), "XAF");
        // B sends 3000 to C
        aggregate.captureEntry(UUID.randomUUID(), "PAY-002", walletB, walletC, new BigDecimal("3000"), "XAF");
        // C sends 1000 to A
        aggregate.captureEntry(UUID.randomUUID(), "PAY-003", walletC, walletA, new BigDecimal("1000"), "XAF");

        aggregate.closeBatch();
        aggregate.calculatePositions();

        assertEquals(BatchStatus.SETTLING, aggregate.getStatus());

        List<SettlementBatchAggregate.NetPositionData> positions = aggregate.getNetPositionsList();
        assertEquals(3, positions.size());

        // A: debit=5000, credit=1000, net=-4000
        SettlementBatchAggregate.NetPositionData posA = positions.stream()
                .filter(p -> p.getWalletId().equals(walletA)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("5000").compareTo(posA.getGrossDebit()));
        assertEquals(0, new BigDecimal("1000").compareTo(posA.getGrossCredit()));
        assertEquals(0, new BigDecimal("-4000").compareTo(posA.getNetAmount()));

        // B: debit=3000, credit=5000, net=+2000
        SettlementBatchAggregate.NetPositionData posB = positions.stream()
                .filter(p -> p.getWalletId().equals(walletB)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("3000").compareTo(posB.getGrossDebit()));
        assertEquals(0, new BigDecimal("5000").compareTo(posB.getGrossCredit()));
        assertEquals(0, new BigDecimal("2000").compareTo(posB.getNetAmount()));

        // C: debit=1000, credit=3000, net=+2000
        SettlementBatchAggregate.NetPositionData posC = positions.stream()
                .filter(p -> p.getWalletId().equals(walletC)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("1000").compareTo(posC.getGrossDebit()));
        assertEquals(0, new BigDecimal("3000").compareTo(posC.getGrossCredit()));
        assertEquals(0, new BigDecimal("2000").compareTo(posC.getNetAmount()));
    }

    @Test
    void applySettlement_shouldRequireSettlingStatus() {
        assertThrows(InvalidBatchStatusException.class, () -> aggregate.applySettlement());
    }

    @Test
    void completeBatch_shouldTransitionToCompleted() {
        aggregate.captureEntry(UUID.randomUUID(), "PAY-001", UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1000"), "XAF");
        aggregate.closeBatch();
        aggregate.calculatePositions();
        aggregate.applySettlement();
        aggregate.completeBatch();

        assertEquals(BatchStatus.COMPLETED, aggregate.getStatus());
        assertNotNull(aggregate.getSettledAt());
    }

    @Test
    void failBatch_shouldTransitionToFailed() {
        aggregate.failBatch("Test failure");

        assertEquals(BatchStatus.FAILED, aggregate.getStatus());
    }

    @Test
    void reconstitute_shouldReplayAllEvents() {
        UUID walletA = UUID.randomUUID();
        UUID walletB = UUID.randomUUID();

        aggregate.captureEntry(UUID.randomUUID(), "PAY-001", walletA, walletB, new BigDecimal("5000"), "XAF");
        aggregate.captureEntry(UUID.randomUUID(), "PAY-002", walletB, walletA, new BigDecimal("2000"), "XAF");

        List<BatchEventEntry> allEvents = aggregate.getUncommittedEvents();

        // Prepend the BATCH_OPENED event (already committed)
        BatchEventEntry openEvent = new BatchEventEntry();
        openEvent.setBatchId(batchId);
        openEvent.setEventType(BatchEventType.BATCH_OPENED);
        openEvent.setReference("BATCH-XAF-20260719-100000");
        openEvent.setCycle(SettlementCycle.HOURLY);
        openEvent.setCurrency("XAF");
        openEvent.setAggregateVersion(1L);
        openEvent.setOccurredAt(aggregate.getOpenedAt());

        List<BatchEventEntry> fullHistory = new java.util.ArrayList<>();
        fullHistory.add(openEvent);
        fullHistory.addAll(allEvents);

        SettlementBatchAggregate reconstituted = SettlementBatchAggregate.reconstitute(fullHistory);

        assertEquals(batchId, reconstituted.getBatchId());
        assertEquals(BatchStatus.COLLECTING, reconstituted.getStatus());
        assertEquals(2, reconstituted.getTotalEntries());
        assertEquals(0, new BigDecimal("7000").compareTo(reconstituted.getTotalAmount()));
        assertEquals(2, reconstituted.getCapturedPaymentIds().size());
    }
}
