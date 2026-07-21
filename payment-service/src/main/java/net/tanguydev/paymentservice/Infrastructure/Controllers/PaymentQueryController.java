package net.tanguydev.paymentservice.Infrastructure.Controllers;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Presenters.PaymentPresenterInterface;
import net.tanguydev.paymentservice.Domain.Responses.PaymentResponse;
import net.tanguydev.paymentservice.Domain.UseCases.FindPaymentByIdUseCaseInterface;
import net.tanguydev.paymentservice.Domain.UseCases.FindPaymentsByWalletUseCaseInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentQueryController {

    private final FindPaymentByIdUseCaseInterface findById;
    private final FindPaymentsByWalletUseCaseInterface findByWallet;
    private final PaymentPresenterInterface presenter;

    public PaymentQueryController(FindPaymentByIdUseCaseInterface findById,
                                  FindPaymentsByWalletUseCaseInterface findByWallet,
                                  PaymentPresenterInterface presenter) {
        this.findById = findById;
        this.findByWallet = findByWallet;
        this.presenter = presenter;
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
