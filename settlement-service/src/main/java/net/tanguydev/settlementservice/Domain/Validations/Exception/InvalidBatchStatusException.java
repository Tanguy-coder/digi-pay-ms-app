package net.tanguydev.settlementservice.Domain.Validations.Exception;

import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;

public class InvalidBatchStatusException extends RuntimeException {

    public InvalidBatchStatusException(BatchStatus current, String expected) {
        super("Invalid batch status: " + current + ", expected: " + expected);
    }
}
