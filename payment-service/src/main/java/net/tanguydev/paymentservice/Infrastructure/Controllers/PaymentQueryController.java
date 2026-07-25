package net.tanguydev.paymentservice.Infrastructure.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Presenters.PaymentPresenterInterface;
import net.tanguydev.paymentservice.Domain.Responses.PaymentResponse;
import net.tanguydev.paymentservice.Domain.UseCases.FindPaymentByIdUseCaseInterface;
import net.tanguydev.paymentservice.Domain.UseCases.FindPaymentsByWalletUseCaseInterface;
import net.tanguydev.paymentservice.Domain.Validations.Exception.PaymentNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Payments")
@RestController
@RequestMapping("/api/v1/payments")
@SecurityRequirement(name = "BearerAuth")
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

    @Operation(summary = "Get a payment by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment found"),
                    @ApiResponse(responseCode = "404", description = "Payment not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> show(@PathVariable UUID id) {
        return findById.execute(id)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Operation(summary = "List payments by wallet",
            responses = @ApiResponse(responseCode = "200", description = "Payment history for the wallet"))
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<PaymentResponse>> showByWallet(@PathVariable UUID walletId) {
        List<DomainPayment> payments = findByWallet.execute(walletId);
        return ResponseEntity.ok(presenter.present(payments));
    }
}
