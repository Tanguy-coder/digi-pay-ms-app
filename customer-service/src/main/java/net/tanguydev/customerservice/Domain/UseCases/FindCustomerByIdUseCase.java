package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.Validations.DomainCustomerValidator;

import java.util.Optional;

public class FindCustomerByIdUseCase implements FindCutomerByIdUseCaseInterface{
    private final CustomerServiceInterface customerService;
    private final DomainCustomerValidator validator = new DomainCustomerValidator();

    public FindCustomerByIdUseCase(CustomerServiceInterface customerService) {
        this.customerService = customerService;
    }

    @Override
    public Optional<DomainCustomer> execute(Long id) {
        //validator.validate(ne);
        return this.customerService.findById(id);
    }
}
