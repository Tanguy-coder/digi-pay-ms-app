package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.Validations.DomainCustomerValidator;
import net.tanguydev.customerservice.Domain.Validations.Exception.CustomerNotFoundException;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UpdateCustomerUseCase implements UpdateCustomerUseCaseInterface {
    private final CustomerServiceInterface customerService;
    private final DomainCustomerValidator validator = new DomainCustomerValidator();

    public UpdateCustomerUseCase(CustomerServiceInterface customerService) {
        this.customerService = customerService;
    }

    @Override
    public DomainCustomer execute(UUID id, DomainCustomer updatedData) {
        DomainCustomer existing = customerService.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        existing.setFirstName(updatedData.getFirstName());
        existing.setLastName(updatedData.getLastName());
        existing.setEmail(updatedData.getEmail());
        existing.setPhoneNumber(updatedData.getPhoneNumber());
        existing.setNationality(updatedData.getNationality());
        existing.setAddressLine1(updatedData.getAddressLine1());
        existing.setCity(updatedData.getCity());
        existing.setCountry(updatedData.getCountry());
        existing.setPreferredCurrency(updatedData.getPreferredCurrency());
        existing.setProfilePictureUrl(updatedData.getProfilePictureUrl());
        existing.setMetadata(updatedData.getMetadata());
        existing.setUpdatedAt(OffsetDateTime.now());

        validator.validate(existing);
        return customerService.save(existing);
    }
}
