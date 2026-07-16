package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;

import java.math.BigDecimal;

public interface CreditWalletUseCaseInterface {
    DomainWallet execute(Long walletId, BigDecimal amount);
}
