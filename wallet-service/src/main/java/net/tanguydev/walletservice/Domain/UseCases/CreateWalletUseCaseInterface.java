package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;

public interface CreateWalletUseCaseInterface {
    DomainWallet execute(DomainWallet wallet);
}
