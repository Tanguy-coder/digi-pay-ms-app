package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Ports.PaymentServiceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FindPaymentsByWalletUseCase implements FindPaymentsByWalletUseCaseInterface {

    private final PaymentServiceInterface paymentService;

    public FindPaymentsByWalletUseCase(PaymentServiceInterface paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public List<DomainPayment> execute(UUID walletId) {
        List<DomainPayment> result = new ArrayList<>();
        result.addAll(paymentService.findBySenderWalletId(walletId));
        result.addAll(paymentService.findByReceiverWalletId(walletId));
        return result;
    }
}
