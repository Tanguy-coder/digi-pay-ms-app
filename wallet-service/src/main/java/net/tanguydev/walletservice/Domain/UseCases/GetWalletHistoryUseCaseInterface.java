package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;

import java.util.List;
import java.util.UUID;

public interface GetWalletHistoryUseCaseInterface {

    List<WalletEventEntry> execute(UUID walletId);
}
