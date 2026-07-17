package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.Validations.DomainWalletValidator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CreateWalletUseCase implements CreateWalletUseCaseInterface {

    private final WalletServiceInterface walletService;
    private final WalletEventPublisherInterface eventPublisher;
    private final DomainWalletValidator validator = new DomainWalletValidator();

    public CreateWalletUseCase(WalletServiceInterface walletService,
                               WalletEventPublisherInterface eventPublisher) {
        this.walletService = walletService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DomainWallet execute(DomainWallet wallet) {
        if (wallet.getBalance() == null) wallet.setBalance(BigDecimal.ZERO);
        if (wallet.getFrozenAmount() == null) wallet.setFrozenAmount(BigDecimal.ZERO);
        if (wallet.getStatus() == null) wallet.setStatus(WalletStatus.ACTIVE);
        if (wallet.getWalletType() == null) wallet.setWalletType(WalletType.PERSONAL);
        if (wallet.getWalletNumber() == null) wallet.setWalletNumber(generateWalletNumber());

        validator.validate(wallet);
        DomainWallet saved = walletService.save(wallet);

        WalletEvent event = new WalletEvent();
        event.setEventType("wallet.created");
        event.setWalletId(saved.getId());
        event.setCustomerId(saved.getCustomerId());
        event.setCurrency(saved.getCurrency());
        event.setAmount(null);
        event.setBalanceAfter(saved.getBalance());
        event.setFrozenAmountAfter(saved.getFrozenAmount());
        event.setStatus(saved.getStatus());
        event.setOccurredAt(OffsetDateTime.now());

        eventPublisher.publish(event);

        return saved;
    }

    private String generateWalletNumber() {
        String digits = String.valueOf(System.currentTimeMillis()).substring(3);
        return "WLT-" + digits;
    }
}
