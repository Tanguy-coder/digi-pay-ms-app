package net.tanguydev.walletservice.Domain.Validations.Exception;

import java.util.Map;

public class DomainValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public DomainValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
