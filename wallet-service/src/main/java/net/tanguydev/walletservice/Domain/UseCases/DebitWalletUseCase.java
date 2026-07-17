package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.Validations.Exception.InsufficientBalanceException;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotActiveException;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotFoundException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DebitWalletUseCase implements DebitWalletUseCaseInterface {

    private final WalletServiceInterface walletService;
    private final WalletEventPublisherInterface eventPublisher;

    public DebitWalletUseCase(WalletServiceInterface walletService,
                              WalletEventPublisherInterface eventPublisher) {
        this.walletService = walletService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DomainWallet execute(UUID walletId, BigDecimal amount) {
        DomainWallet wallet = walletService.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(walletId);
        }

        BigDecimal available = wallet.getAvailableBalance();
        if (amount.compareTo(available) > 0) {
            throw new InsufficientBalanceException(amount, available);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(OffsetDateTime.now());

        DomainWallet saved = walletService.save(wallet);

        WalletEvent event = new WalletEvent();
        event.setEventType("wallet.debited");
        event.setWalletId(saved.getId());
        event.setCustomerId(saved.getCustomerId());
        event.setCurrency(saved.getCurrency());
        event.setAmount(amount);
        event.setBalanceAfter(saved.getBalance());
        event.setFrozenAmountAfter(saved.getFrozenAmount());
        event.setStatus(saved.getStatus());
        event.setOccurredAt(OffsetDateTime.now());

        eventPublisher.publish(event);

        return saved;
    }
}
