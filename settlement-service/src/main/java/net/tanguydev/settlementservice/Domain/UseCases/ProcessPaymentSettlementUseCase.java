package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Enums.EntryType;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Enums.SettlementStatus;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEventPublisherInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementRepositoryInterface;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ProcessPaymentSettlementUseCase implements ProcessPaymentSettlementUseCaseInterface {

    private final SettlementRepositoryInterface settlementRepository;
    private final SettlementEntryRepositoryInterface entryRepository;
    private final SettlementEventPublisherInterface eventPublisher;

    public ProcessPaymentSettlementUseCase(
            SettlementRepositoryInterface settlementRepository,
            SettlementEntryRepositoryInterface entryRepository,
            SettlementEventPublisherInterface eventPublisher) {
        this.settlementRepository = settlementRepository;
        this.entryRepository = entryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute(ProcessPaymentSettlementCommand command) {
        if (entryRepository.existsByPaymentId(command.getPaymentId())) {
            return;
        }

        DomainSettlement settlement = buildSettlement(command);
        settlement = settlementRepository.save(settlement);

        DomainSettlementEntry debitEntry = buildEntry(settlement.getId(), command, EntryType.DEBIT, command.getSenderWalletId());
        DomainSettlementEntry creditEntry = buildEntry(settlement.getId(), command, EntryType.CREDIT, command.getReceiverWalletId());

        entryRepository.save(debitEntry);
        entryRepository.save(creditEntry);

        settlement.setNetPosition(BigDecimal.ZERO);
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setSettledAt(OffsetDateTime.now());
        settlement.setEntries(List.of(debitEntry, creditEntry));
        settlement = settlementRepository.save(settlement);

        eventPublisher.publishSettlementCompleted(settlement);
    }

    private DomainSettlement buildSettlement(ProcessPaymentSettlementCommand command) {
        DomainSettlement settlement = new DomainSettlement();
        settlement.setId(UUID.randomUUID());
        settlement.setReference("SET-" + command.getPaymentReference());
        settlement.setStatus(SettlementStatus.PROCESSING);
        settlement.setCycle(SettlementCycle.MANUAL);
        settlement.setCurrency(command.getCurrency());
        settlement.setTotalPayments(1);
        settlement.setTotalAmount(command.getAmount());
        settlement.setNetPosition(BigDecimal.ZERO);
        settlement.setPeriodStart(OffsetDateTime.now());
        settlement.setPeriodEnd(OffsetDateTime.now());
        settlement.setCreatedAt(OffsetDateTime.now());
        return settlement;
    }

    private DomainSettlementEntry buildEntry(UUID settlementId, ProcessPaymentSettlementCommand command,
                                              EntryType type, UUID walletId) {
        DomainSettlementEntry entry = new DomainSettlementEntry();
        entry.setId(UUID.randomUUID());
        entry.setSettlementId(settlementId);
        entry.setPaymentId(command.getPaymentId());
        entry.setPaymentReference(command.getPaymentReference());
        entry.setWalletId(walletId);
        entry.setEntryType(type);
        entry.setAmount(command.getAmount());
        entry.setCurrency(command.getCurrency());
        return entry;
    }
}
