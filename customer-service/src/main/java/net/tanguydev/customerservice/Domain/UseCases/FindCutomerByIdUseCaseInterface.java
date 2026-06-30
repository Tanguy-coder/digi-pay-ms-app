package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;

import java.util.Optional;

public interface FindCutomerByIdUseCaseInterface {
    Optional<DomainCustomer> execute(Long id);
}
