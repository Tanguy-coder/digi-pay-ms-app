package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;

import java.util.Optional;
import java.util.UUID;

public interface FindPaymentByIdUseCaseInterface {
    Optional<DomainPayment> execute(UUID id);
}
