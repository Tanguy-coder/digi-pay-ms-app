package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.Validations.DomainCustomerValidator;

public class CreateCustomerUseCase implements CreateCustomerUseCaseInterface{
    private final CustomerServiceInterface customerService;
    private final DomainCustomerValidator validator = new DomainCustomerValidator();

    public CreateCustomerUseCase(CustomerServiceInterface customerService) {
        this.customerService = customerService;
    }

    @Override
    public DomainCustomer execute(DomainCustomer customer) {
        validator.validate(customer);
        return this.customerService.save(customer);
    }
}
