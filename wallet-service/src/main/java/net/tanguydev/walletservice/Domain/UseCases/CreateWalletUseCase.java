package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.Validations.DomainWalletValidator;

import java.math.BigDecimal;

public class CreateWalletUseCase implements CreateWalletUseCaseInterface {

    private final WalletServiceInterface walletService;
    private final DomainWalletValidator validator = new DomainWalletValidator();

    public CreateWalletUseCase(WalletServiceInterface walletService) {
        this.walletService = walletService;
    }

    @Override
    public DomainWallet execute(DomainWallet wallet) {
        if (wallet.getBalance() == null) wallet.setBalance(BigDecimal.ZERO);
        if (wallet.getFrozenAmount() == null) wallet.setFrozenAmount(BigDecimal.ZERO);
        if (wallet.getStatus() == null) wallet.setStatus(WalletStatus.ACTIVE);

        validator.validate(wallet);
        return walletService.save(wallet);
    }
}
