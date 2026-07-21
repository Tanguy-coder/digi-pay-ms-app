package net.tanguydev.settlementservice.Domain.Aggregates;

import net.tanguydev.settlementservice.Domain.Enums.BatchEventType;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.NetPositionStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;
import net.tanguydev.settlementservice.Domain.Validations.Exception.DuplicateEntryException;
import net.tanguydev.settlementservice.Domain.Validations.Exception.InvalidBatchStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public class SettlementBatchAggregate {

    private UUID batchId;
    private String reference;
    private BatchStatus status;
    private SettlementCycle cycle;
    private String currency;
    private int totalEntries;
    private BigDecimal totalAmount;
    private OffsetDateTime openedAt;
    private OffsetDateTime closedAt;
    private OffsetDateTime settledAt;
    private long version;

    private final Set<UUID> capturedPaymentIds = new HashSet<>();
    private final Map<UUID, NetPositionData> netPositions = new HashMap<>();
    private final List<BatchEventEntry> uncommittedEvents = new ArrayList<>();

    public SettlementBatchAggregate() {
        this.totalEntries = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.version = 0;
    }

    public static SettlementBatchAggregate reconstitute(List<BatchEventEntry> events) {
        SettlementBatchAggregate aggregate = new SettlementBatchAggregate();
        for (BatchEventEntry event : events) {
            aggregate.apply(event);
            aggregate.version = event.getAggregateVersion();
        }
        return aggregate;
    }

    public void openBatch(UUID batchId, String reference, SettlementCycle cycle, String currency) {
        BatchEventEntry event = new BatchEventEntry();
        event.setBatchId(batchId);
        event.setEventType(BatchEventType.BATCH_OPENED);
        event.setReference(reference);
        event.setCycle(cycle);
        event.setCurrency(currency);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    public void captureEntry(UUID paymentId, String paymentReference, UUID senderWalletId,
                             UUID receiverWalletId, BigDecimal amount, String currency) {
        requireOpenOrCollecting();

        if (capturedPaymentIds.contains(paymentId)) {
            throw new DuplicateEntryException(paymentId);
        }

        BatchEventEntry event = new BatchEventEntry();
        event.setBatchId(this.batchId);
        event.setEventType(BatchEventType.ENTRY_CAPTURED);
        event.setPaymentId(paymentId);
        event.setPaymentReference(paymentReference);
        event.setSenderWalletId(senderWalletId);
        event.setReceiverWalletId(receiverWalletId);
        event.setAmount(amount);
        event.setCurrency(currency);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    public void closeBatch() {
        requireOpenOrCollecting();

        BatchEventEntry event = new BatchEventEntry();
        event.setBatchId(this.batchId);
        event.setEventType(BatchEventType.BATCH_CLOSED);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    public void calculatePositions() {
        requireStatus(BatchStatus.CALCULATING);

        for (Map.Entry<UUID, NetPositionData> entry : netPositions.entrySet()) {
            UUID walletId = entry.getKey();
            NetPositionData data = entry.getValue();
            BigDecimal net = data.grossCredit.subtract(data.grossDebit);

            BatchEventEntry event = new BatchEventEntry();
            event.setBatchId(this.batchId);
            event.setEventType(BatchEventType.POSITIONS_CALCULATED);
            event.setWalletId(walletId);
            event.setGrossDebit(data.grossDebit);
            event.setGrossCredit(data.grossCredit);
            event.setNetAmount(net);
            event.setPositionStatus(NetPositionStatus.CALCULATED);
            event.setAggregateVersion(version + 1);
            event.setOccurredAt(OffsetDateTime.now());

            apply(event);
            uncommittedEvents.add(event);
        }
    }

    public void applySettlement() {
        requireStatus(BatchStatus.SETTLING);

        BatchEventEntry event = new BatchEventEntry();
        event.setBatchId(this.batchId);
        event.setEventType(BatchEventType.SETTLEMENT_APPLIED);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    public void completeBatch() {
        requireStatus(BatchStatus.SETTLING);

        BatchEventEntry event = new BatchEventEntry();
        event.setBatchId(this.batchId);
        event.setEventType(BatchEventType.BATCH_COMPLETED);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    public void failBatch(String reason) {
        BatchEventEntry event = new BatchEventEntry();
        event.setBatchId(this.batchId);
        event.setEventType(BatchEventType.BATCH_FAILED);
        event.setReason(reason);
        event.setAggregateVersion(version + 1);
        event.setOccurredAt(OffsetDateTime.now());

        apply(event);
        uncommittedEvents.add(event);
    }

    private void apply(BatchEventEntry event) {
        switch (event.getEventType()) {
            case BATCH_OPENED -> {
                this.batchId = event.getBatchId();
                this.reference = event.getReference();
                this.cycle = event.getCycle();
                this.currency = event.getCurrency();
                this.status = BatchStatus.OPEN;
                this.openedAt = event.getOccurredAt();
                this.totalEntries = 0;
                this.totalAmount = BigDecimal.ZERO;
            }
            case ENTRY_CAPTURED -> {
                this.capturedPaymentIds.add(event.getPaymentId());
                this.totalEntries++;
                this.totalAmount = this.totalAmount.add(event.getAmount());
                if (this.status == BatchStatus.OPEN) {
                    this.status = BatchStatus.COLLECTING;
                }
                trackPosition(event.getSenderWalletId(), event.getAmount(), BigDecimal.ZERO);
                trackPosition(event.getReceiverWalletId(), BigDecimal.ZERO, event.getAmount());
            }
            case BATCH_CLOSED -> {
                this.status = BatchStatus.CALCULATING;
                this.closedAt = event.getOccurredAt();
            }
            case POSITIONS_CALCULATED -> {
                NetPositionData data = netPositions.computeIfAbsent(event.getWalletId(), k -> new NetPositionData());
                data.grossDebit = event.getGrossDebit();
                data.grossCredit = event.getGrossCredit();
                data.netAmount = event.getNetAmount();
                data.status = event.getPositionStatus();
                this.status = BatchStatus.SETTLING;
            }
            case SETTLEMENT_APPLIED -> {
                for (NetPositionData data : netPositions.values()) {
                    data.status = NetPositionStatus.SETTLED;
                }
            }
            case BATCH_COMPLETED -> {
                this.status = BatchStatus.COMPLETED;
                this.settledAt = event.getOccurredAt();
            }
            case BATCH_FAILED -> {
                this.status = BatchStatus.FAILED;
            }
        }
        this.version = event.getAggregateVersion();
    }

    private void trackPosition(UUID walletId, BigDecimal debit, BigDecimal credit) {
        NetPositionData data = netPositions.computeIfAbsent(walletId, k -> new NetPositionData());
        data.grossDebit = data.grossDebit.add(debit);
        data.grossCredit = data.grossCredit.add(credit);
    }

    private void requireOpenOrCollecting() {
        if (status != BatchStatus.OPEN && status != BatchStatus.COLLECTING) {
            throw new InvalidBatchStatusException(status, "OPEN or COLLECTING");
        }
    }

    private void requireStatus(BatchStatus required) {
        if (status != required) {
            throw new InvalidBatchStatusException(status, required.name());
        }
    }

    public List<BatchEventEntry> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }

    public void markEventsCommitted() {
        uncommittedEvents.clear();
    }

    public UUID getBatchId() {
        return batchId;
    }

    public String getReference() {
        return reference;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public SettlementCycle getCycle() {
        return cycle;
    }

    public String getCurrency() {
        return currency;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public OffsetDateTime getSettledAt() {
        return settledAt;
    }

    public long getVersion() {
        return version;
    }

    public Set<UUID> getCapturedPaymentIds() {
        return Collections.unmodifiableSet(capturedPaymentIds);
    }

    public List<NetPositionData> getNetPositionsList() {
        return netPositions.entrySet().stream()
                .map(e -> {
                    NetPositionData data = e.getValue();
                    data.walletId = e.getKey();
                    return data;
                })
                .toList();
    }

    public static class NetPositionData {
        private UUID walletId;
        private BigDecimal grossDebit = BigDecimal.ZERO;
        private BigDecimal grossCredit = BigDecimal.ZERO;
        private BigDecimal netAmount = BigDecimal.ZERO;
        private NetPositionStatus status;

        public UUID getWalletId() {
            return walletId;
        }

        public BigDecimal getGrossDebit() {
            return grossDebit;
        }

        public BigDecimal getGrossCredit() {
            return grossCredit;
        }

        public BigDecimal getNetAmount() {
            return netAmount;
        }

        public NetPositionStatus getStatus() {
            return status;
        }
    }
}
