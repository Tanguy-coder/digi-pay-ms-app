package net.tanguydev.walletservice.Infrastructure.Config;

import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.UseCases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public CreateWalletUseCase createWalletUseCase(WalletServiceInterface walletService) {
        return new CreateWalletUseCase(walletService);
    }

    @Bean
    public FindWalletByIdUseCase findWalletByIdUseCase(WalletServiceInterface walletService) {
        return new FindWalletByIdUseCase(walletService);
    }

    @Bean
    public FindWalletByCustomerIdUseCase findWalletByCustomerIdUseCase(WalletServiceInterface walletService) {
        return new FindWalletByCustomerIdUseCase(walletService);
    }

    @Bean
    public CreditWalletUseCase creditWalletUseCase(WalletServiceInterface walletService) {
        return new CreditWalletUseCase(walletService);
    }

    @Bean
    public DebitWalletUseCase debitWalletUseCase(WalletServiceInterface walletService) {
        return new DebitWalletUseCase(walletService);
    }

    @Bean
    public FreezeAmountUseCase freezeAmountUseCase(WalletServiceInterface walletService) {
        return new FreezeAmountUseCase(walletService);
    }
}
