package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;

import java.util.Optional;
import java.util.UUID;

public interface FindWalletByIdUseCaseInterface {
    Optional<DomainWallet> execute(UUID id);
}
