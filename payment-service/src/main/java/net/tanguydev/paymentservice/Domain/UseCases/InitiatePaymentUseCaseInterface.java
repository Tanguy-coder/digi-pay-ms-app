package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;

public interface InitiatePaymentUseCaseInterface {
    DomainPayment execute(DomainPayment payment);
}
