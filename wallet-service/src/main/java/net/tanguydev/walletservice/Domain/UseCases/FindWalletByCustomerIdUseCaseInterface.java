package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;

import java.util.Optional;

public interface FindWalletByCustomerIdUseCaseInterface {
    Optional<DomainWallet> execute(Long customerId);
}
