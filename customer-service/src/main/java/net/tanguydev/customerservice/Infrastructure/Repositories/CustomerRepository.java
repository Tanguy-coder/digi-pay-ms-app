package net.tanguydev.customerservice.Infrastructure.Repositories;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Gateways.CustomerRepositoryInterface;
import net.tanguydev.customerservice.Infrastructure.Mappers.CustomerMapper;
import net.tanguydev.customerservice.Infrastructure.Models.Customer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerRepository implements CustomerRepositoryInterface {
    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerMapper customerMapper;

    public CustomerRepository(CustomerJpaRepository customerJpaRepository, CustomerMapper customerMapper) {
        this.customerJpaRepository = customerJpaRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    public DomainCustomer save(DomainCustomer customer) {
        Customer customerToSave = this.customerJpaRepository.save(customerMapper.toJpa(customer));
        return customerMapper.toDomain(customerToSave);
    }

    @Override
    public Optional<DomainCustomer> findById(UUID id) {
        return this.customerJpaRepository.findById(id).map(customerMapper::toDomain);
    }

    @Override
    public Optional<DomainCustomer> findByEmail(String email) {
        return this.customerJpaRepository.findByEmail(email).map(customerMapper::toDomain);
    }

    @Override
    public Optional<DomainCustomer> findByPhoneNumber(String phoneNumber) {
        return this.customerJpaRepository.findByPhoneNumber(phoneNumber).map(customerMapper::toDomain);
    }

    @Override
    public List<DomainCustomer> findAll() {
        List<Customer> customers = this.customerJpaRepository.findAll();
        return customerMapper.toDomainList(customers);
    }
}
