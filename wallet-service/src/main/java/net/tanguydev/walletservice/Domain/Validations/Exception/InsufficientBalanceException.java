package net.tanguydev.walletservice.Domain.Validations.Exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(BigDecimal requested, BigDecimal available) {
        super("Insufficient balance: requested " + requested + " but available " + available);
    }
}
