package net.tanguydev.paymentservice.Domain.Validations;

public interface Validator<T> {
    void validate(T entity);
}
