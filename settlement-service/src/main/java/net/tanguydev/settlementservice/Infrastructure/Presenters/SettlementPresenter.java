package net.tanguydev.settlementservice.Infrastructure.Presenters;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
import net.tanguydev.settlementservice.Domain.Presenters.SettlementPresenterInterface;
import net.tanguydev.settlementservice.Domain.Responses.SettlementResponse;
import net.tanguydev.settlementservice.Infrastructure.Mappers.SettlementMapper;
public class SettlementPresenter implements SettlementPresenterInterface {

    private final SettlementMapper mapper;

    public SettlementPresenter(SettlementMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SettlementResponse present(DomainSettlement settlement) {
        SettlementResponse response = mapper.toResponse(settlement);
        if (settlement.getEntries() != null) {
            response.setEntries(mapper.toEntryResponseList(settlement.getEntries()));
        }
        return response;
    }
}
