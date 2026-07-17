package net.tanguydev.walletservice.Infrastructure.Config;

import net.tanguydev.walletservice.Infrastructure.Mappers.WalletMapper;
import net.tanguydev.walletservice.Infrastructure.Presenters.WalletPresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresentationConfig {

    @Bean
    public WalletPresenter walletPresenter(WalletMapper walletMapper) {
        return new WalletPresenter(walletMapper);
    }
}
