package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Enums.AccountStatus;
import net.tanguydev.customerservice.Domain.Enums.KycStatus;
import net.tanguydev.customerservice.Domain.Enums.TierLevel;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.Validations.DomainCustomerValidator;

import java.math.BigDecimal;

public class CreateCustomerUseCase implements CreateCustomerUseCaseInterface{
    private final CustomerServiceInterface customerService;
    private final DomainCustomerValidator validator = new DomainCustomerValidator();

    public CreateCustomerUseCase(CustomerServiceInterface customerService) {
        this.customerService = customerService;
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
        return this.customerService.save(customer);
    }
}
