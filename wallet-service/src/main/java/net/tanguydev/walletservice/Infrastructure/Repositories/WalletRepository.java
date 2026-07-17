package net.tanguydev.walletservice.Infrastructure.Repositories;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Gateways.WalletRepositoryInterface;
import net.tanguydev.walletservice.Infrastructure.Mappers.WalletMapper;
import net.tanguydev.walletservice.Infrastructure.Models.Wallet;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WalletRepository implements WalletRepositoryInterface {

    private final WalletJpaRepository walletJpaRepository;
    private final WalletMapper walletMapper;

    public WalletRepository(WalletJpaRepository walletJpaRepository, WalletMapper walletMapper) {
        this.walletJpaRepository = walletJpaRepository;
        this.walletMapper = walletMapper;
    }

    @Override
    public DomainWallet save(DomainWallet wallet) {
        Wallet saved = walletJpaRepository.save(walletMapper.toJpa(wallet));
        return walletMapper.toDomain(saved);
    }

    @Override
    public Optional<DomainWallet> findById(UUID id) {
        return walletJpaRepository.findById(id).map(walletMapper::toDomain);
    }

    @Override
    public Optional<DomainWallet> findByCustomerId(UUID customerId) {
        return walletJpaRepository.findByCustomerId(customerId).map(walletMapper::toDomain);
    }

    @Override
    public List<DomainWallet> findAll() {
        return walletMapper.toDomainList(walletJpaRepository.findAll());
    }
}
