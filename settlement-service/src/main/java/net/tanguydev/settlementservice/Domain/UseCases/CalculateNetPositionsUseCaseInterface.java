package net.tanguydev.settlementservice.Domain.UseCases;

import java.util.UUID;

public interface CalculateNetPositionsUseCaseInterface {

    void execute(UUID batchId);
}
