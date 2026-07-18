package net.tanguydev.settlementservice.Domain.Ports;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;

public interface SettlementEventPublisherInterface {
    void publishSettlementCompleted(DomainSettlement settlement);
    void publishSettlementFailed(DomainSettlement settlement, String reason);
}
