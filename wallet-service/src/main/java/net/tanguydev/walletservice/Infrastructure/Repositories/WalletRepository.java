package net.tanguydev.walletservice.Infrastructure.Repositories;

import jakarta.persistence.EntityManager;
import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Gateways.WalletRepositoryInterface;
import net.tanguydev.walletservice.Infrastructure.Mappers.WalletMapper;
import net.tanguydev.walletservice.Infrastructure.Models.Wallet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WalletRepository implements WalletRepositoryInterface {

    private final WalletJpaRepository walletJpaRepository;
    private final WalletMapper walletMapper;
    private final EntityManager entityManager;

    public WalletRepository(WalletJpaRepository walletJpaRepository, WalletMapper walletMapper, EntityManager entityManager) {
        this.walletJpaRepository = walletJpaRepository;
        this.walletMapper = walletMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public DomainWallet save(DomainWallet wallet) {
        Wallet entity = walletMapper.toJpa(wallet);
        if (wallet.getId() == null) {
            entity.setId(null);
            entity.setVersion(0L);
            entityManager.persist(entity);
            entityManager.flush();
        } else {
            entity = entityManager.merge(entity);
        }
        return walletMapper.toDomain(entity);
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
