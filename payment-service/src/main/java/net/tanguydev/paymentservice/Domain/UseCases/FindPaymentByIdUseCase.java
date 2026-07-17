package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Ports.PaymentServiceInterface;

import java.util.Optional;
import java.util.UUID;

public class FindPaymentByIdUseCase implements FindPaymentByIdUseCaseInterface {

    private final PaymentServiceInterface paymentService;

    public FindPaymentByIdUseCase(PaymentServiceInterface paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Optional<DomainPayment> execute(UUID id) {
        return paymentService.findById(id);
    }
}
