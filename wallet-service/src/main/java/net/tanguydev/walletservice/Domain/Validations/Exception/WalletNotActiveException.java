package net.tanguydev.walletservice.Domain.Validations.Exception;

public class WalletNotActiveException extends RuntimeException {

    public WalletNotActiveException(Long walletId) {
        super("Wallet " + walletId + " is not active");
    }
}
