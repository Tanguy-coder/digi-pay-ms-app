package net.tanguydev.walletservice.Infrastructure.Adapters;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Gateways.WalletRepositoryInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService implements WalletServiceInterface {

    private final WalletRepositoryInterface walletRepository;

    public WalletService(WalletRepositoryInterface walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public DomainWallet save(DomainWallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public Optional<DomainWallet> findById(UUID id) {
        return walletRepository.findById(id);
    }

    @Override
    public Optional<DomainWallet> findByCustomerId(UUID customerId) {
        return walletRepository.findByCustomerId(customerId);
    }

    @Override
    public List<DomainWallet> findAll() {
        return walletRepository.findAll();
    }
}
