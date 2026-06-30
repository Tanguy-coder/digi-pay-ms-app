package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.Validations.DomainCustomerValidator;

import java.util.List;

public class ListCustomerUseCase implements ListCustomersUseCaseInterface{
    private final CustomerServiceInterface customerService;
    private final DomainCustomerValidator validator = new DomainCustomerValidator();

    public ListCustomerUseCase(CustomerServiceInterface customerService) {
        this.customerService = customerService;
    }

    @Override
    public List<DomainCustomer> execute() {
        return this.customerService.findAll();
    }
}
