package net.tanguydev.paymentservice.Domain.Presenters;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Responses.PaymentResponse;

import java.util.List;

public interface PaymentPresenterInterface {

    PaymentResponse present(DomainPayment payment);

    List<PaymentResponse> present(List<DomainPayment> payments);
}
