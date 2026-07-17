package net.tanguydev.walletservice.Domain.Validations.Exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(UUID id) {
        super("Wallet not found with id: " + id);
    }
}
