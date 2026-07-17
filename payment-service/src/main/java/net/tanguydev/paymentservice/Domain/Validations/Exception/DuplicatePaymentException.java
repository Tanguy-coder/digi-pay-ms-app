package net.tanguydev.paymentservice.Domain.Validations.Exception;

public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(String idempotencyKey) {
        super("Payment already exists for idempotency key: " + idempotencyKey);
    }
}
