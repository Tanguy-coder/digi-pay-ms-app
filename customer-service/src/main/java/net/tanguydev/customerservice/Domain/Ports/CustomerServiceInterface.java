package net.tanguydev.customerservice.Domain.Ports;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerServiceInterface {
    DomainCustomer save(DomainCustomer customer);
    Optional<DomainCustomer> findById(UUID id);
    Optional<DomainCustomer> findByEmail(String email);
    Optional<DomainCustomer> findByPhoneNumber(String phoneNumber);
    List<DomainCustomer> findAll();
}
