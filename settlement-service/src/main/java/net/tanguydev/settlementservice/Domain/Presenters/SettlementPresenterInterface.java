package net.tanguydev.settlementservice.Domain.Presenters;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
import net.tanguydev.settlementservice.Domain.Responses.SettlementResponse;

public interface SettlementPresenterInterface {
    SettlementResponse present(DomainSettlement settlement);
}
