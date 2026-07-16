package net.tanguydev.walletservice.Domain.Ports;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;

import java.util.List;
import java.util.Optional;

public interface WalletServiceInterface {

    DomainWallet save(DomainWallet wallet);

    Optional<DomainWallet> findById(Long id);

    Optional<DomainWallet> findByCustomerId(Long customerId);

    List<DomainWallet> findAll();
}
