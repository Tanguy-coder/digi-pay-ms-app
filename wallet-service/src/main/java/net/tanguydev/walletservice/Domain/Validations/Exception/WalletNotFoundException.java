package net.tanguydev.walletservice.Domain.Validations.Exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(Long id) {
        super("Wallet not found with id: " + id);
    }
}
