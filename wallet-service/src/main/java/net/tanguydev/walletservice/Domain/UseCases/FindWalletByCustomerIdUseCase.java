package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;

import java.util.Optional;
import java.util.UUID;

public class FindWalletByCustomerIdUseCase implements FindWalletByCustomerIdUseCaseInterface {

    private final WalletServiceInterface walletService;

    public FindWalletByCustomerIdUseCase(WalletServiceInterface walletService) {
        this.walletService = walletService;
    }

    @Override
    public Optional<DomainWallet> execute(UUID customerId) {
        return walletService.findByCustomerId(customerId);
    }
}
