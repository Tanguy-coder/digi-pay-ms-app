package net.tanguydev.settlementservice.Infrastructure.Controllers;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Presenters.BatchPresenterInterface;
import net.tanguydev.settlementservice.Domain.Responses.BatchResponse;
import net.tanguydev.settlementservice.Domain.UseCases.CloseBatchUseCaseInterface;
import net.tanguydev.settlementservice.Domain.UseCases.OpenBatchUseCaseInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/settlements/batches")
public class SettlementCommandController {

    private final OpenBatchUseCaseInterface openBatchUseCase;
    private final CloseBatchUseCaseInterface closeBatchUseCase;
    private final BatchPresenterInterface presenter;

    public SettlementCommandController(OpenBatchUseCaseInterface openBatchUseCase,
                                       CloseBatchUseCaseInterface closeBatchUseCase,
                                       BatchPresenterInterface presenter) {
        this.openBatchUseCase = openBatchUseCase;
        this.closeBatchUseCase = closeBatchUseCase;
        this.presenter = presenter;
    }

    @PostMapping("/open")
    @Transactional
    public ResponseEntity<BatchResponse> openBatch(
            @RequestParam(defaultValue = "HOURLY") SettlementCycle cycle,
            @RequestParam(defaultValue = "XAF") String currency) {
        DomainSettlementBatch batch = openBatchUseCase.execute(cycle, currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(presenter.present(batch));
    }

    @PostMapping("/{id}/close")
    @Transactional
    public ResponseEntity<Void> closeBatch(@PathVariable UUID id) {
        closeBatchUseCase.execute(id);
        return ResponseEntity.ok().build();
    }
}
