package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditWalletUseCaseInterface {
    DomainWallet execute(UUID walletId, BigDecimal amount);
}
