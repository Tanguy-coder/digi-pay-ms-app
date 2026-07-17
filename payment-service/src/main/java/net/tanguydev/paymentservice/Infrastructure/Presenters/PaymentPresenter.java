package net.tanguydev.paymentservice.Infrastructure.Presenters;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Presenters.PaymentPresenterInterface;
import net.tanguydev.paymentservice.Domain.Responses.PaymentResponse;
import net.tanguydev.paymentservice.Infrastructure.Mappers.PaymentMapper;

import java.util.List;

public class PaymentPresenter implements PaymentPresenterInterface {

    private final PaymentMapper mapper;

    public PaymentPresenter(PaymentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PaymentResponse present(DomainPayment payment) {
        return mapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> present(List<DomainPayment> payments) {
        return mapper.toResponseList(payments);
    }
}
