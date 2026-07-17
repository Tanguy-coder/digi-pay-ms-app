package net.tanguydev.walletservice.Domain.Validations;

public interface Validator<T> {
    void validate(T t);
}
