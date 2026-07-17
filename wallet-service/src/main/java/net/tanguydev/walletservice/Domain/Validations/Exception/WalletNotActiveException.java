package net.tanguydev.walletservice.Domain.Validations.Exception;

import java.util.UUID;

public class WalletNotActiveException extends RuntimeException {

    public WalletNotActiveException(UUID walletId) {
        super("Wallet " + walletId + " is not active");
    }
}
