package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;

import java.util.List;
import java.util.UUID;

public interface FindPaymentsByWalletUseCaseInterface {
    List<DomainPayment> execute(UUID walletId);
}
