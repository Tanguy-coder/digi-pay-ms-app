package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;

import java.util.Optional;
import java.util.UUID;

public interface FindCutomerByIdUseCaseInterface {
    Optional<DomainCustomer> execute(UUID id);
}
