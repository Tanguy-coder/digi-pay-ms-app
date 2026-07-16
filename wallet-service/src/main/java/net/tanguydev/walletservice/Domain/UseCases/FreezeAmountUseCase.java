package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.Validations.Exception.InsufficientBalanceException;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotActiveException;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotFoundException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class FreezeAmountUseCase implements FreezeAmountUseCaseInterface {

    private final WalletServiceInterface walletService;

    public FreezeAmountUseCase(WalletServiceInterface walletService) {
        this.walletService = walletService;
    }

    @Override
    public DomainWallet execute(Long walletId, BigDecimal amount) {
        DomainWallet wallet = walletService.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(walletId);
        }

        BigDecimal available = wallet.getAvailableBalance();
        if (amount.compareTo(available) > 0) {
            throw new InsufficientBalanceException(amount, available);
        }

        wallet.setFrozenAmount(wallet.getFrozenAmount().add(amount));
        wallet.setUpdatedAt(OffsetDateTime.now());

        return walletService.save(wallet);
    }
}
