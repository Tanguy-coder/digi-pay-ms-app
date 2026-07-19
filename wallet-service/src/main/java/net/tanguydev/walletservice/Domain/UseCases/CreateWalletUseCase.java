package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Aggregates.WalletAggregate;
import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Ports.EventStoreInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.Validations.DomainWalletValidator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CreateWalletUseCase implements CreateWalletUseCaseInterface {

    private final WalletServiceInterface walletService;
    private final WalletEventPublisherInterface eventPublisher;
    private final EventStoreInterface eventStore;
    private final DomainWalletValidator validator = new DomainWalletValidator();

    public CreateWalletUseCase(WalletServiceInterface walletService,
                               WalletEventPublisherInterface eventPublisher,
                               EventStoreInterface eventStore) {
        this.walletService = walletService;
        this.eventPublisher = eventPublisher;
        this.eventStore = eventStore;
    }

    @Override
    public DomainWallet execute(DomainWallet wallet) {
        if (wallet.getBalance() == null) wallet.setBalance(BigDecimal.ZERO);
        if (wallet.getFrozenAmount() == null) wallet.setFrozenAmount(BigDecimal.ZERO);
        if (wallet.getStatus() == null) wallet.setStatus(WalletStatus.ACTIVE);
        if (wallet.getWalletType() == null) wallet.setWalletType(WalletType.PERSONAL);
        if (wallet.getWalletNumber() == null) wallet.setWalletNumber(generateWalletNumber());

        validator.validate(wallet);

        UUID walletId = UUID.randomUUID();

        WalletAggregate aggregate = new WalletAggregate();
        aggregate.createWallet(walletId, wallet.getCustomerId(), wallet.getWalletNumber(),
                wallet.getWalletType(), wallet.getCurrency(),
                wallet.getDailyLimit(), wallet.getMonthlyLimit());

        for (WalletEventEntry event : aggregate.getUncommittedEvents()) {
            eventStore.append(event);
        }
        aggregate.markEventsCommitted();

        DomainWallet projection = toDomainWallet(aggregate);
        DomainWallet saved = walletService.save(projection);

        WalletEvent kafkaEvent = new WalletEvent();
        kafkaEvent.setEventType("wallet.created");
        kafkaEvent.setWalletId(saved.getId());
        kafkaEvent.setCustomerId(saved.getCustomerId());
        kafkaEvent.setCurrency(saved.getCurrency());
        kafkaEvent.setAmount(null);
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

    private String generateWalletNumber() {
        String digits = String.valueOf(System.currentTimeMillis()).substring(3);
        return "WLT-" + digits;
    }
}
