package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Aggregates.WalletAggregate;
import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Ports.EventStoreInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotFoundException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class FreezeAmountUseCase implements FreezeAmountUseCaseInterface {

    private final WalletServiceInterface walletService;
    private final WalletEventPublisherInterface eventPublisher;
    private final EventStoreInterface eventStore;

    public FreezeAmountUseCase(WalletServiceInterface walletService,
                               WalletEventPublisherInterface eventPublisher,
                               EventStoreInterface eventStore) {
        this.walletService = walletService;
        this.eventPublisher = eventPublisher;
        this.eventStore = eventStore;
    }

    @Override
    public DomainWallet execute(UUID walletId, BigDecimal amount) {
        List<WalletEventEntry> events = eventStore.loadEvents(walletId);
        if (events.isEmpty()) {
            throw new WalletNotFoundException(walletId);
        }

        WalletAggregate aggregate = WalletAggregate.reconstitute(events);
        aggregate.freezeAmount(amount);

        for (WalletEventEntry event : aggregate.getUncommittedEvents()) {
            eventStore.append(event);
        }
        aggregate.markEventsCommitted();

        DomainWallet projection = toDomainWallet(aggregate);
        DomainWallet saved = walletService.save(projection);

        WalletEvent kafkaEvent = new WalletEvent();
        kafkaEvent.setEventType("wallet.amount_frozen");
        kafkaEvent.setWalletId(saved.getId());
        kafkaEvent.setCustomerId(saved.getCustomerId());
        kafkaEvent.setCurrency(saved.getCurrency());
        kafkaEvent.setAmount(amount);
        kafkaEvent.setBalanceAfter(saved.getBalance());
        kafkaEvent.setFrozenAmountAfter(saved.getFrozenAmount());
        kafkaEvent.setStatus(saved.getStatus());
        kafkaEvent.setOccurredAt(OffsetDateTime.now());

        eventPublisher.publish(kafkaEvent);

        return saved;
    }

    private DomainWallet toDomainWallet(WalletAggregate aggregate) {
        DomainWallet w = new DomainWallet();
        w.setId(aggregate.getWalletId());
        w.setCustomerId(aggregate.getCustomerId());
        w.setWalletNumber(aggregate.getWalletNumber());
        w.setWalletType(aggregate.getWalletType());
        w.setCurrency(aggregate.getCurrency());
        w.setBalance(aggregate.getBalance());
        w.setFrozenAmount(aggregate.getFrozenAmount());
        w.setDailyLimit(aggregate.getDailyLimit());
        w.setMonthlyLimit(aggregate.getMonthlyLimit());
        w.setStatus(aggregate.getStatus());
        w.setVersion(aggregate.getVersion());
        w.setCreatedAt(aggregate.getCreatedAt());
        w.setUpdatedAt(aggregate.getUpdatedAt());
        return w;
    }
}
