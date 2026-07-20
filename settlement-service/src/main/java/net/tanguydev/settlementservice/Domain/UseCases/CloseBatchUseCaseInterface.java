package net.tanguydev.settlementservice.Domain.UseCases;

import java.util.UUID;

public interface CloseBatchUseCaseInterface {

    void execute(UUID batchId);
}
