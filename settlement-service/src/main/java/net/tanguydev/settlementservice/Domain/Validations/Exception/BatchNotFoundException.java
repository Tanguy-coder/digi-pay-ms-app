package net.tanguydev.settlementservice.Domain.Validations.Exception;

import java.util.UUID;

public class BatchNotFoundException extends RuntimeException {

    public BatchNotFoundException(UUID batchId) {
        super("Settlement batch not found: " + batchId);
    }
}
