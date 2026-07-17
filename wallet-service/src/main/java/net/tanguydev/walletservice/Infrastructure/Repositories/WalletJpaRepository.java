package net.tanguydev.walletservice.Infrastructure.Repositories;

import net.tanguydev.walletservice.Infrastructure.Models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByCustomerIdAndWalletType(UUID customerId,
            net.tanguydev.walletservice.Domain.Enums.WalletType walletType);

    Optional<Wallet> findByCustomerId(UUID customerId);

    Optional<Wallet> findByWalletNumber(String walletNumber);
}
