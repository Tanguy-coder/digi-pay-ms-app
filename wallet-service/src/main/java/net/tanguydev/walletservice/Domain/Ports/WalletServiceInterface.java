package net.tanguydev.walletservice.Domain.Ports;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletServiceInterface {

    DomainWallet save(DomainWallet wallet);

    Optional<DomainWallet> findById(UUID id);

    Optional<DomainWallet> findByCustomerId(UUID customerId);

    List<DomainWallet> findAll();
}
