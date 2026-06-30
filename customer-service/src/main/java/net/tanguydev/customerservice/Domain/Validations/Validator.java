package net.tanguydev.customerservice.Domain.Validations;

public interface Validator<T> {
    void validate(T t);
}
