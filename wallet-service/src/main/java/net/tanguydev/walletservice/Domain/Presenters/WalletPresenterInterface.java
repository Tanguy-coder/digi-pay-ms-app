package net.tanguydev.walletservice.Domain.Presenters;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Responses.WalletResponse;

import java.util.List;

public interface WalletPresenterInterface {

    WalletResponse present(DomainWallet wallet);

    List<WalletResponse> present(List<DomainWallet> wallets);
}
