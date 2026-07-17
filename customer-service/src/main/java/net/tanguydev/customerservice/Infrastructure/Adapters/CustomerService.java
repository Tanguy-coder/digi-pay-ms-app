package net.tanguydev.customerservice.Infrastructure.Adapters;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Gateways.CustomerRepositoryInterface;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService implements CustomerServiceInterface {
    private  final CustomerRepositoryInterface customerRepository;

    public CustomerService(CustomerRepositoryInterface customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public DomainCustomer save(DomainCustomer customer) {

        return this.customerRepository.save(customer);
    }

    @Override
    public Optional<DomainCustomer> findById(UUID id) {
        return this.customerRepository.findById(id);
    }

    @Override
    public Optional<DomainCustomer> findByEmail(String email) {
        return this.customerRepository.findByEmail(email);
    }

    @Override
    public Optional<DomainCustomer> findByPhoneNumber(String phoneNumber) {
        return this.customerRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public List<DomainCustomer> findAll() {
        return this.customerRepository.findAll();
    }
}
