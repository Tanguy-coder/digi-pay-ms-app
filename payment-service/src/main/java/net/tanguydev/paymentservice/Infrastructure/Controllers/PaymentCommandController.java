package net.tanguydev.paymentservice.Infrastructure.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Payments")
@RestController
@RequestMapping("/api/v1/payments")
@SecurityRequirement(name = "BearerAuth")
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

    @Operation(summary = "Initiate a payment",
            description = "Starts a Saga: FRAUD_CHECK → DEBIT → CREDIT. Returns 201 immediately with status INITIATED. "
                    + "Provide a unique idempotencyKey — replaying the same key returns 409.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Payment initiated (Saga started)"),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "409", description = "Duplicate idempotency key")
            })
    @PostMapping
    @Transactional
    public ResponseEntity<PaymentResponse> store(@Valid @RequestBody InitiatePaymentRequest request) {
        DomainPayment domain = mapper.requestToDomain(request);
        DomainPayment created = initiate.execute(domain);
        return ResponseEntity.status(201).body(presenter.present(created));
    }
}
