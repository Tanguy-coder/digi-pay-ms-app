package net.tanguydev.settlementservice.Infrastructure.Controllers;

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

@RestController
@RequestMapping("/api/settlements/batches")
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

    @GetMapping
    public ResponseEntity<List<BatchResponse>> getAllBatches() {
        List<BatchResponse> responses = batchRepository.findAll().stream()
                .map(presenter::present)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/current")
    public ResponseEntity<BatchResponse> getCurrentBatch(@RequestParam(defaultValue = "XAF") String currency) {
        return batchRepository.findCurrentOpenBatch(currency)
                .map(this::enrichBatch)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchResponse> getBatchById(@PathVariable UUID id) {
        return batchRepository.findById(id)
                .map(this::enrichBatch)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/entries")
    public ResponseEntity<List<DomainSettlementEntry>> getBatchEntries(@PathVariable UUID id) {
        batchRepository.findById(id).orElseThrow(() -> new BatchNotFoundException(id));
        return ResponseEntity.ok(entryRepository.findByBatchId(id));
    }

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
