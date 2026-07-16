package net.tanguydev.walletservice.Infrastructure.Repositories;

import net.tanguydev.walletservice.Infrastructure.Models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByCustomerId(Long customerId);
}
