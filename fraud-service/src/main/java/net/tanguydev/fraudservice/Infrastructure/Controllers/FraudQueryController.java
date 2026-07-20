package net.tanguydev.fraudservice.Infrastructure.Controllers;

import net.tanguydev.fraudservice.Domain.Ports.FraudAnalysisRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Presenters.FraudAnalysisPresenterInterface;
import net.tanguydev.fraudservice.Domain.Responses.FraudAnalysisResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud-analyses")
public class FraudQueryController {

    private final FraudAnalysisRepositoryInterface repository;
    private final FraudAnalysisPresenterInterface presenter;

    public FraudQueryController(FraudAnalysisRepositoryInterface repository,
                                FraudAnalysisPresenterInterface presenter) {
        this.repository = repository;
        this.presenter = presenter;
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<FraudAnalysisResponse> showByPayment(@PathVariable UUID paymentId) {
        return repository.findByPaymentId(paymentId)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<FraudAnalysisResponse>> showByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(presenter.present(repository.findByCustomerId(customerId)));
    }
}
