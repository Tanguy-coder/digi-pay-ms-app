package net.tanguydev.settlementservice.Domain.UseCases;

import java.util.UUID;

public interface CompleteBatchUseCaseInterface {

    void execute(UUID batchId);
}
