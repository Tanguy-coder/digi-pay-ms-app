package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;

import java.util.UUID;

public interface UpdateCustomerUseCaseInterface {
    DomainCustomer execute(UUID id, DomainCustomer customer);
}
