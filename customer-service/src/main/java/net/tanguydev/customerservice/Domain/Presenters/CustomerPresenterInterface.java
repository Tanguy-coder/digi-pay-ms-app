package net.tanguydev.customerservice.Domain.Presenters;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Responses.CustomerResponse;

import java.util.List;

public interface CustomerPresenterInterface {
    CustomerResponse present(DomainCustomer customer);
    List<CustomerResponse> present(List<DomainCustomer> customers);
}
