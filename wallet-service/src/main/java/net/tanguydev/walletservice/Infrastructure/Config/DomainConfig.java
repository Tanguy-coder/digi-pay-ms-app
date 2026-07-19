package net.tanguydev.walletservice.Infrastructure.Config;

import net.tanguydev.walletservice.Domain.Ports.EventStoreInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.UseCases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public CreateWalletUseCase createWalletUseCase(WalletServiceInterface walletService,
                                                   WalletEventPublisherInterface eventPublisher,
                                                   EventStoreInterface eventStore) {
        return new CreateWalletUseCase(walletService, eventPublisher, eventStore);
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
                                                   WalletEventPublisherInterface eventPublisher,
                                                   EventStoreInterface eventStore) {
        return new CreditWalletUseCase(walletService, eventPublisher, eventStore);
    }

    @Bean
    public DebitWalletUseCase debitWalletUseCase(WalletServiceInterface walletService,
                                                 WalletEventPublisherInterface eventPublisher,
                                                 EventStoreInterface eventStore) {
        return new DebitWalletUseCase(walletService, eventPublisher, eventStore);
    }

    @Bean
    public FreezeAmountUseCase freezeAmountUseCase(WalletServiceInterface walletService,
                                                   WalletEventPublisherInterface eventPublisher,
                                                   EventStoreInterface eventStore) {
        return new FreezeAmountUseCase(walletService, eventPublisher, eventStore);
    }

    @Bean
    public GetWalletHistoryUseCase getWalletHistoryUseCase(EventStoreInterface eventStore) {
        return new GetWalletHistoryUseCase(eventStore);
    }
}
