package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotActiveException;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotFoundException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CreditWalletUseCase implements CreditWalletUseCaseInterface {

    private final WalletServiceInterface walletService;

    public CreditWalletUseCase(WalletServiceInterface walletService) {
        this.walletService = walletService;
    }

    @Override
    public DomainWallet execute(Long walletId, BigDecimal amount) {
        DomainWallet wallet = walletService.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(walletId);
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(OffsetDateTime.now());

        return walletService.save(wallet);
    }
}
