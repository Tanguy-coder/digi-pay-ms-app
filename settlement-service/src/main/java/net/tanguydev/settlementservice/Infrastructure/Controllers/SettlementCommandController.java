package net.tanguydev.settlementservice.Infrastructure.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Settlement Batches")
@RestController
@RequestMapping("/api/settlements/batches")
@SecurityRequirement(name = "BearerAuth")
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

    @Operation(summary = "Open a settlement batch",
            description = "Opens a new batch for the given cycle and currency. Only one batch can be open at a time per currency.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Batch opened"),
                    @ApiResponse(responseCode = "409", description = "A batch is already open for this currency")
            })
    @PostMapping("/open")
    @Transactional
    public ResponseEntity<BatchResponse> openBatch(
            @RequestParam(defaultValue = "HOURLY") SettlementCycle cycle,
            @RequestParam(defaultValue = "XAF") String currency) {
        DomainSettlementBatch batch = openBatchUseCase.execute(cycle, currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(presenter.present(batch));
    }

    @Operation(summary = "Close a settlement batch",
            description = "Closes the batch and triggers net position calculation. The hourly scheduler also closes batches automatically.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Batch closed"),
                    @ApiResponse(responseCode = "404", description = "Batch not found"),
                    @ApiResponse(responseCode = "409", description = "Batch already closed")
            })
    @PostMapping("/{id}/close")
    @Transactional
    public ResponseEntity<Void> closeBatch(@PathVariable UUID id) {
        closeBatchUseCase.execute(id);
        return ResponseEntity.ok().build();
    }
}
