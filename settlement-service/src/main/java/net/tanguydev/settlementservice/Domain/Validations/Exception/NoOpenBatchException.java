package net.tanguydev.settlementservice.Domain.Validations.Exception;

public class NoOpenBatchException extends RuntimeException {

    public NoOpenBatchException(String currency) {
        super("No open settlement batch for currency: " + currency);
    }
}
