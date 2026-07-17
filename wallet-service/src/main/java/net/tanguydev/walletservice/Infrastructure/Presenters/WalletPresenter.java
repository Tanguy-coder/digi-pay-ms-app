package net.tanguydev.walletservice.Infrastructure.Presenters;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Presenters.WalletPresenterInterface;
import net.tanguydev.walletservice.Domain.Responses.WalletResponse;
import net.tanguydev.walletservice.Infrastructure.Mappers.WalletMapper;

import java.util.List;

public class WalletPresenter implements WalletPresenterInterface {

    private final WalletMapper walletMapper;

    public WalletPresenter(WalletMapper walletMapper) {
        this.walletMapper = walletMapper;
    }

    @Override
    public WalletResponse present(DomainWallet wallet) {
        return walletMapper.toResponse(wallet);
    }

    @Override
    public List<WalletResponse> present(List<DomainWallet> wallets) {
        return walletMapper.toResponseList(wallets);
    }
}
