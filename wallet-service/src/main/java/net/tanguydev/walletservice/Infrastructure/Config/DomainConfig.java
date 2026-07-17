package net.tanguydev.walletservice.Infrastructure.Config;

import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.UseCases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public CreateWalletUseCase createWalletUseCase(WalletServiceInterface walletService,
                                                   WalletEventPublisherInterface eventPublisher) {
        return new CreateWalletUseCase(walletService, eventPublisher);
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
    public CreditWalletUseCase creditWalletUseCase(WalletServiceInterface walletService,
                                                   WalletEventPublisherInterface eventPublisher) {
        return new CreditWalletUseCase(walletService, eventPublisher);
    }

    @Bean
    public DebitWalletUseCase debitWalletUseCase(WalletServiceInterface walletService,
                                                 WalletEventPublisherInterface eventPublisher) {
        return new DebitWalletUseCase(walletService, eventPublisher);
    }

    @Bean
    public FreezeAmountUseCase freezeAmountUseCase(WalletServiceInterface walletService,
                                                   WalletEventPublisherInterface eventPublisher) {
        return new FreezeAmountUseCase(walletService, eventPublisher);
    }
}
