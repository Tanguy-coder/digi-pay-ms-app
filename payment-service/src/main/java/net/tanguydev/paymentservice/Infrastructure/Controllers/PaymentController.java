package net.tanguydev.paymentservice.Infrastructure.Controllers;

import jakarta.validation.Valid;
import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Presenters.PaymentPresenterInterface;
import net.tanguydev.paymentservice.Domain.Responses.PaymentResponse;
import net.tanguydev.paymentservice.Domain.UseCases.FindPaymentByIdUseCaseInterface;
import net.tanguydev.paymentservice.Domain.UseCases.FindPaymentsByWalletUseCaseInterface;
import net.tanguydev.paymentservice.Domain.UseCases.InitiatePaymentUseCaseInterface;
import net.tanguydev.paymentservice.Infrastructure.Mappers.PaymentMapper;
import net.tanguydev.paymentservice.Infrastructure.Requests.InitiatePaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final InitiatePaymentUseCaseInterface initiate;
    private final FindPaymentByIdUseCaseInterface findById;
    private final FindPaymentsByWalletUseCaseInterface findByWallet;
    private final PaymentPresenterInterface presenter;
    private final PaymentMapper mapper;

    public PaymentController(InitiatePaymentUseCaseInterface initiate,
                             FindPaymentByIdUseCaseInterface findById,
                             FindPaymentsByWalletUseCaseInterface findByWallet,
                             PaymentPresenterInterface presenter,
                             PaymentMapper mapper) {
        this.initiate = initiate;
        this.findById = findById;
        this.findByWallet = findByWallet;
        this.presenter = presenter;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> store(@Valid @RequestBody InitiatePaymentRequest request) {
        DomainPayment domain = mapper.requestToDomain(request);
        DomainPayment created = initiate.execute(domain);
        return ResponseEntity.status(201).body(presenter.present(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> show(@PathVariable UUID id) {
        return findById.execute(id)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<PaymentResponse>> showByWallet(@PathVariable UUID walletId) {
        List<DomainPayment> payments = findByWallet.execute(walletId);
        return ResponseEntity.ok(presenter.present(payments));
    }
}
