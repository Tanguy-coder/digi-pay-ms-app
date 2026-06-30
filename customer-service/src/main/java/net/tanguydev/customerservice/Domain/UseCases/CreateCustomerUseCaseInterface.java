package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;

public interface CreateCustomerUseCaseInterface {
    DomainCustomer execute(DomainCustomer customer);
}
