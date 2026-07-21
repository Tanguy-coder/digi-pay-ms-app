package net.tanguydev.settlementservice.Domain.UseCases;

import java.util.UUID;

public interface ApplySettlementUseCaseInterface {

    void execute(UUID batchId);
}
