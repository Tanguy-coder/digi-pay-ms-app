package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;

import java.util.Optional;

public class FindWalletByCustomerIdUseCase implements FindWalletByCustomerIdUseCaseInterface {

    private final WalletServiceInterface walletService;

    public FindWalletByCustomerIdUseCase(WalletServiceInterface walletService) {
        this.walletService = walletService;
    }

    @Override
    public Optional<DomainWallet> execute(Long customerId) {
        return walletService.findByCustomerId(customerId);
    }
}
