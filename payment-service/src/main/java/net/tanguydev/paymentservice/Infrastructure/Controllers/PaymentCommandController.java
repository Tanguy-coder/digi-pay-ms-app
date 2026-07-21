package net.tanguydev.paymentservice.Infrastructure.Controllers;

import jakarta.validation.Valid;
import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Presenters.PaymentPresenterInterface;
import net.tanguydev.paymentservice.Domain.Responses.PaymentResponse;
import net.tanguydev.paymentservice.Domain.UseCases.InitiatePaymentUseCaseInterface;
import net.tanguydev.paymentservice.Infrastructure.Mappers.PaymentMapper;
import net.tanguydev.paymentservice.Infrastructure.Requests.InitiatePaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentCommandController {

    private final InitiatePaymentUseCaseInterface initiate;
    private final PaymentPresenterInterface presenter;
    private final PaymentMapper mapper;

    public PaymentCommandController(InitiatePaymentUseCaseInterface initiate,
                                    PaymentPresenterInterface presenter,
                                    PaymentMapper mapper) {
        this.initiate = initiate;
        this.presenter = presenter;
        this.mapper = mapper;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<PaymentResponse> store(@Valid @RequestBody InitiatePaymentRequest request) {
        DomainPayment domain = mapper.requestToDomain(request);
        DomainPayment created = initiate.execute(domain);
        return ResponseEntity.status(201).body(presenter.present(created));
    }
}
