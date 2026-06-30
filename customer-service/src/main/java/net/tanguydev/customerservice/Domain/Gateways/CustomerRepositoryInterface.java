package net.tanguydev.customerservice.Domain.Gateways;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryInterface {
    DomainCustomer save(DomainCustomer customer);
    Optional<DomainCustomer> findById(Long id);
    Optional<DomainCustomer> findByEmail(String email);
    Optional<DomainCustomer> findByPhoneNumber(String phoneNumber);
    List<DomainCustomer> findAll();
}
