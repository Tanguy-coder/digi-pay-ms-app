package net.tanguydev.settlementservice.Domain.Presenters;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Responses.BatchResponse;

public interface BatchPresenterInterface {

    BatchResponse present(DomainSettlementBatch batch);
}
