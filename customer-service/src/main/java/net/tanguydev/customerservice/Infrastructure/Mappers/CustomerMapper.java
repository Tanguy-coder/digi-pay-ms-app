package net.tanguydev.customerservice.Infrastructure.Mappers;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Responses.CustomerResponse;
import net.tanguydev.customerservice.Infrastructure.Models.Customer;
import net.tanguydev.customerservice.Infrastructure.Requests.CustomerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CustomerMapper {
    DomainCustomer toDomain(Customer customer);
    Customer toJpa(DomainCustomer customer);
    List<DomainCustomer> toDomainList(List<Customer> customers);
    List<Customer> toJpaList(List<DomainCustomer> customers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "kycStatus", ignore = true)
    @Mapping(target = "kycVerifiedAt", ignore = true)
    @Mapping(target = "riskScore", ignore = true)
    @Mapping(target = "tierLevel", ignore = true)
    @Mapping(target = "dailyLimit", ignore = true)
    @Mapping(target = "isEmailVerified", ignore = true)
    @Mapping(target = "isPhoneVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    DomainCustomer requestToDomain(CustomerRequest request);

    CustomerResponse toResponse(DomainCustomer customer);
    List<CustomerResponse> toResponseList(List<DomainCustomer> customers);
    DomainCustomer toDomainResponse(CustomerResponse customerResponse);
    List<DomainCustomer> toDomainResponseList(List<CustomerResponse> customerResponseList);
}
