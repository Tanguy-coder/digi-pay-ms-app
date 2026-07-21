package net.tanguydev.settlementservice.Infrastructure.Schedulers;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.UseCases.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatchScheduler.class);

    private final OpenBatchUseCaseInterface openBatch;
    private final CloseBatchUseCaseInterface closeBatch;
    private final CalculateNetPositionsUseCaseInterface calculatePositions;
    private final ApplySettlementUseCaseInterface applySettlement;
    private final CompleteBatchUseCaseInterface completeBatch;
    private final SettlementBatchRepositoryInterface batchRepository;

    @Value("${settlement.batch.currencies:XAF}")
    private List<String> currencies;

    public BatchScheduler(OpenBatchUseCaseInterface openBatch,
                          CloseBatchUseCaseInterface closeBatch,
                          CalculateNetPositionsUseCaseInterface calculatePositions,
                          ApplySettlementUseCaseInterface applySettlement,
                          CompleteBatchUseCaseInterface completeBatch,
                          SettlementBatchRepositoryInterface batchRepository) {
        this.openBatch = openBatch;
        this.closeBatch = closeBatch;
        this.calculatePositions = calculatePositions;
        this.applySettlement = applySettlement;
        this.completeBatch = completeBatch;
        this.batchRepository = batchRepository;
    }

    @Scheduled(cron = "${settlement.batch.cron:0 0 * * * *}")
    public void runSettlementCycle() {
        log.info("Starting settlement cycle");

        List<DomainSettlementBatch> collectingBatches = batchRepository.findByStatus(BatchStatus.COLLECTING);
        collectingBatches.addAll(batchRepository.findByStatus(BatchStatus.OPEN));
        for (DomainSettlementBatch batch : collectingBatches) {
            try {
                closeBatch.execute(batch.getId());
                log.info("Closed batch {}", batch.getReference());
            } catch (Exception e) {
                log.error("Failed to close batch {}: {}", batch.getReference(), e.getMessage());
            }
        }

        List<DomainSettlementBatch> calculatingBatches = batchRepository.findByStatus(BatchStatus.CALCULATING);
        for (DomainSettlementBatch batch : calculatingBatches) {
            try {
                calculatePositions.execute(batch.getId());
                log.info("Calculated positions for batch {}", batch.getReference());
            } catch (Exception e) {
                log.error("Failed to calculate positions for batch {}: {}", batch.getReference(), e.getMessage());
            }
        }

        List<DomainSettlementBatch> settlingBatches = batchRepository.findByStatus(BatchStatus.SETTLING);
        for (DomainSettlementBatch batch : settlingBatches) {
            try {
                applySettlement.execute(batch.getId());
                completeBatch.execute(batch.getId());
                log.info("Completed batch {}", batch.getReference());
            } catch (Exception e) {
                log.error("Failed to complete batch {}: {}", batch.getReference(), e.getMessage());
            }
        }

        for (String currency : currencies) {
            try {
                if (batchRepository.findCurrentOpenBatch(currency).isEmpty()) {
                    openBatch.execute(SettlementCycle.HOURLY, currency);
                    log.info("Opened new batch for currency {}", currency);
                }
            } catch (Exception e) {
                log.error("Failed to open batch for currency {}: {}", currency, e.getMessage());
            }
        }

        log.info("Settlement cycle completed");
    }
}
