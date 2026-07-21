package net.tanguydev.settlementservice.Infrastructure.Config;

import net.tanguydev.settlementservice.Domain.Ports.*;
import net.tanguydev.settlementservice.Domain.UseCases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class DomainConfig {

    @Bean
    public OpenBatchUseCaseInterface openBatchUseCase(
            BatchEventStoreInterface eventStore,
            SettlementBatchRepositoryInterface batchRepository) {
        return new OpenBatchUseCase(eventStore, batchRepository);
    }

    @Bean
    public CaptureEntryUseCaseInterface captureEntryUseCase(
            BatchEventStoreInterface eventStore,
            SettlementBatchRepositoryInterface batchRepository,
            SettlementEntryRepositoryInterface entryRepository) {
        return new CaptureEntryUseCase(eventStore, batchRepository, entryRepository);
    }

    @Bean
    public CloseBatchUseCaseInterface closeBatchUseCase(
            BatchEventStoreInterface eventStore,
            SettlementBatchRepositoryInterface batchRepository) {
        return new CloseBatchUseCase(eventStore, batchRepository);
    }

    @Bean
    public CalculateNetPositionsUseCaseInterface calculateNetPositionsUseCase(
            BatchEventStoreInterface eventStore,
            SettlementBatchRepositoryInterface batchRepository,
            NetPositionRepositoryInterface netPositionRepository) {
        return new CalculateNetPositionsUseCase(eventStore, batchRepository, netPositionRepository);
    }

    @Bean
    public ApplySettlementUseCaseInterface applySettlementUseCase(
            BatchEventStoreInterface eventStore,
            SettlementBatchRepositoryInterface batchRepository,
            NetPositionRepositoryInterface netPositionRepository) {
        return new ApplySettlementUseCase(eventStore, batchRepository, netPositionRepository);
    }

    @Bean
    public CompleteBatchUseCaseInterface completeBatchUseCase(
            BatchEventStoreInterface eventStore,
            SettlementBatchRepositoryInterface batchRepository,
            SettlementEventPublisherInterface eventPublisher) {
        return new CompleteBatchUseCase(eventStore, batchRepository, eventPublisher);
    }
}
