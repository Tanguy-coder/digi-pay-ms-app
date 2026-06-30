package net.tanguydev.customerservice.Infrastructure.Presenters;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Presenters.CustomerPresenterInterface;
import net.tanguydev.customerservice.Domain.Responses.CustomerResponse;
import net.tanguydev.customerservice.Infrastructure.Mappers.CustomerMapper;

import java.util.List;

public class CustomerPresenter implements CustomerPresenterInterface {
    private final CustomerMapper customerMapper;

    public CustomerPresenter(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @Override
    public CustomerResponse present(DomainCustomer customer) {
        return this.customerMapper.toResponse(customer);
    }

    @Override
    public List<CustomerResponse> present(List<DomainCustomer> customers) {
        return this.customerMapper.toResponseList(customers);
    }
}
