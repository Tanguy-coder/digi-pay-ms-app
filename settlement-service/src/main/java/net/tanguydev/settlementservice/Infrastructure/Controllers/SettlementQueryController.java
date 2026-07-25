package net.tanguydev.settlementservice.Infrastructure.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Ports.NetPositionRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Presenters.BatchPresenterInterface;
import net.tanguydev.settlementservice.Domain.Responses.BatchResponse;
import net.tanguydev.settlementservice.Domain.Validations.Exception.BatchNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Settlement Batches")
@RestController
@RequestMapping("/api/settlements/batches")
@SecurityRequirement(name = "BearerAuth")
public class SettlementQueryController {

    private final SettlementBatchRepositoryInterface batchRepository;
    private final SettlementEntryRepositoryInterface entryRepository;
    private final NetPositionRepositoryInterface positionRepository;
    private final BatchPresenterInterface presenter;

    public SettlementQueryController(SettlementBatchRepositoryInterface batchRepository,
                                     SettlementEntryRepositoryInterface entryRepository,
                                     NetPositionRepositoryInterface positionRepository,
                                     BatchPresenterInterface presenter) {
        this.batchRepository = batchRepository;
        this.entryRepository = entryRepository;
        this.positionRepository = positionRepository;
        this.presenter = presenter;
    }

    @Operation(summary = "List all settlement batches",
            responses = @ApiResponse(responseCode = "200", description = "All batches"))
    @GetMapping
    public ResponseEntity<List<BatchResponse>> getAllBatches() {
        List<BatchResponse> responses = batchRepository.findAll().stream()
                .map(presenter::present)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get the current open batch for a currency",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Open batch found"),
                    @ApiResponse(responseCode = "404", description = "No open batch for this currency")
            })
    @GetMapping("/current")
    public ResponseEntity<BatchResponse> getCurrentBatch(@RequestParam(defaultValue = "XAF") String currency) {
        return batchRepository.findCurrentOpenBatch(currency)
                .map(this::enrichBatch)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a batch by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Batch found"),
                    @ApiResponse(responseCode = "404", description = "Batch not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<BatchResponse> getBatchById(@PathVariable UUID id) {
        return batchRepository.findById(id)
                .map(this::enrichBatch)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "List entries (captured payments) in a batch",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Batch entries"),
                    @ApiResponse(responseCode = "404", description = "Batch not found")
            })
    @GetMapping("/{id}/entries")
    public ResponseEntity<List<DomainSettlementEntry>> getBatchEntries(@PathVariable UUID id) {
        batchRepository.findById(id).orElseThrow(() -> new BatchNotFoundException(id));
        return ResponseEntity.ok(entryRepository.findByBatchId(id));
    }

    @Operation(summary = "Get net positions for a batch",
            description = "Net positions are calculated when the batch is closed. Each position shows the net amount owed between two wallets.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Net positions"),
                    @ApiResponse(responseCode = "404", description = "Batch not found")
            })
    @GetMapping("/{id}/positions")
    public ResponseEntity<List<DomainNetPosition>> getBatchPositions(@PathVariable UUID id) {
        batchRepository.findById(id).orElseThrow(() -> new BatchNotFoundException(id));
        return ResponseEntity.ok(positionRepository.findByBatchId(id));
    }

    private DomainSettlementBatch enrichBatch(DomainSettlementBatch batch) {
        batch.setEntries(entryRepository.findByBatchId(batch.getId()));
        batch.setPositions(positionRepository.findByBatchId(batch.getId()));
        return batch;
    }
}
