package net.tanguydev.settlementservice.Infrastructure.Config;

import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEventPublisherInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementRepositoryInterface;
import net.tanguydev.settlementservice.Domain.UseCases.ProcessPaymentSettlementUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public ProcessPaymentSettlementUseCase processPaymentSettlementUseCase(
            SettlementRepositoryInterface settlementRepository,
            SettlementEntryRepositoryInterface entryRepository,
            SettlementEventPublisherInterface eventPublisher) {
        return new ProcessPaymentSettlementUseCase(settlementRepository, entryRepository, eventPublisher);
    }
}
