package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Enums.AccountStatus;
import net.tanguydev.customerservice.Domain.Enums.KycStatus;
import net.tanguydev.customerservice.Domain.Enums.TierLevel;
import net.tanguydev.customerservice.Domain.Events.CustomerEvent;
import net.tanguydev.customerservice.Domain.Ports.CustomerEventPublisherInterface;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.Validations.DomainCustomerValidator;

import java.math.BigDecimal;

public class CreateCustomerUseCase implements CreateCustomerUseCaseInterface {
    private final CustomerServiceInterface customerService;
    private final CustomerEventPublisherInterface eventPublisher;
    private final DomainCustomerValidator validator = new DomainCustomerValidator();

    public CreateCustomerUseCase(CustomerServiceInterface customerService,
                                 CustomerEventPublisherInterface eventPublisher) {
        this.customerService = customerService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DomainCustomer execute(DomainCustomer customer) {
        if (customer != null) {
            if (customer.getStatus() == null) customer.setStatus(AccountStatus.PENDING);
            if (customer.getKycStatus() == null) customer.setKycStatus(KycStatus.NOT_SUBMITTED);
            if (customer.getTierLevel() == null) customer.setTierLevel(TierLevel.BASIC);
            if (customer.getRiskScore() == null) customer.setRiskScore(BigDecimal.ZERO);
            if (customer.getIsEmailVerified() == null) customer.setIsEmailVerified(false);
            if (customer.getIsPhoneVerified() == null) customer.setIsPhoneVerified(false);
        }

        validator.validate(customer);
        DomainCustomer saved = this.customerService.save(customer);

        CustomerEvent event = new CustomerEvent();
        event.setEventType("customer.created");
        event.setCustomerId(saved.getId());
        event.setFirstName(saved.getFirstName());
        event.setLastName(saved.getLastName());
        event.setEmail(saved.getEmail());
        event.setPhoneNumber(saved.getPhoneNumber());
        event.setNationality(saved.getNationality());
        event.setCountry(saved.getCountry());
        event.setStatus(saved.getStatus());
        event.setKycStatus(saved.getKycStatus());
        event.setTierLevel(saved.getTierLevel());
        event.setPreferredCurrency(saved.getPreferredCurrency());

        eventPublisher.publish(event);

        return saved;
    }
}
