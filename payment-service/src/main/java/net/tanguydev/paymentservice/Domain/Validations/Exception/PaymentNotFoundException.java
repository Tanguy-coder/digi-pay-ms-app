package net.tanguydev.paymentservice.Domain.Validations.Exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(UUID id) {
        super("Payment not found with id: " + id);
    }
}
