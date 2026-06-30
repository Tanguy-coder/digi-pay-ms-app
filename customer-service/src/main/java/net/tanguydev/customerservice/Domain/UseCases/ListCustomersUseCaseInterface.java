package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;

import java.util.List;

public interface ListCustomersUseCaseInterface {
    List<DomainCustomer> execute();
}
