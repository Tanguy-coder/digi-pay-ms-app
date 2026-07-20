package net.tanguydev.settlementservice.Domain.Ports;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;

public interface SettlementEventPublisherInterface {

    void publishBatchCompleted(DomainSettlementBatch batch);

    void publishBatchFailed(DomainSettlementBatch batch, String reason);
}
