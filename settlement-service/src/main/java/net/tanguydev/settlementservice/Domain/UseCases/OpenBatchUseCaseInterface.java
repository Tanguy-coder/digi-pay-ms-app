package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;

public interface OpenBatchUseCaseInterface {

    DomainSettlementBatch execute(SettlementCycle cycle, String currency);
}
