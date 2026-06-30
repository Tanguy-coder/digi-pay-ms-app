package net.tanguydev.customerservice.Infrastructure.Mappers;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Responses.CustomerResponse;
import net.tanguydev.customerservice.Infrastructure.Models.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CustomerMapper {
    DomainCustomer toDomain(Customer customer);
    @Mapping(target = "id", ignore = true)
    Customer toJpa(DomainCustomer customer);
    List<DomainCustomer> toDomainList(List<Customer> customers);
    List<Customer> toJpaList(List<DomainCustomer> customers);

    CustomerResponse toResponse(DomainCustomer customer);
    List<CustomerResponse> toResponseList(List<DomainCustomer> customers);
    DomainCustomer toDomainResponse(CustomerResponse customerResponse);
    List<DomainCustomer> toDomainResponseList(List<CustomerResponse> customerResponseList);
}
