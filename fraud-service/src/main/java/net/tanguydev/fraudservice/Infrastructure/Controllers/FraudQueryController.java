package net.tanguydev.fraudservice.Infrastructure.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.tanguydev.fraudservice.Domain.Ports.FraudAnalysisRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Presenters.FraudAnalysisPresenterInterface;
import net.tanguydev.fraudservice.Domain.Responses.FraudAnalysisResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Fraud Analyses")
@RestController
@RequestMapping("/api/v1/fraud-analyses")
@SecurityRequirement(name = "BearerAuth")
public class FraudQueryController {

    private final FraudAnalysisRepositoryInterface repository;
    private final FraudAnalysisPresenterInterface presenter;

    public FraudQueryController(FraudAnalysisRepositoryInterface repository,
                                FraudAnalysisPresenterInterface presenter) {
        this.repository = repository;
        this.presenter = presenter;
    }

    @Operation(summary = "Get fraud analysis by payment ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Analysis found"),
                    @ApiResponse(responseCode = "404", description = "No analysis for this payment")
            })
    @GetMapping("/{paymentId}")
    public ResponseEntity<FraudAnalysisResponse> showByPayment(@PathVariable UUID paymentId) {
        return repository.findByPaymentId(paymentId)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "List fraud analyses by customer",
            responses = @ApiResponse(responseCode = "200", description = "Fraud history for the customer"))
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<FraudAnalysisResponse>> showByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(presenter.present(repository.findByCustomerId(customerId)));
    }
}
