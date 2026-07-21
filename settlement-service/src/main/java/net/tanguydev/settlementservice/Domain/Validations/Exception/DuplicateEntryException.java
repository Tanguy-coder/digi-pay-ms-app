package net.tanguydev.settlementservice.Domain.Validations.Exception;

import java.util.UUID;

public class DuplicateEntryException extends RuntimeException {

    public DuplicateEntryException(UUID paymentId) {
        super("Payment already captured in batch: " + paymentId);
    }
}
