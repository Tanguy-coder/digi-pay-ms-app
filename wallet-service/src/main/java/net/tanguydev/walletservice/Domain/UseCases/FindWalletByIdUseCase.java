package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;

import java.util.Optional;
import java.util.UUID;

public class FindWalletByIdUseCase implements FindWalletByIdUseCaseInterface {

    private final WalletServiceInterface walletService;

    public FindWalletByIdUseCase(WalletServiceInterface walletService) {
        this.walletService = walletService;
    }

    @Override
    public Optional<DomainWallet> execute(UUID id) {
        return walletService.findById(id);
    }
}
