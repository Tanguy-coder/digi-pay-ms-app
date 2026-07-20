package net.tanguydev.settlementservice.Infrastructure.Controllers;

import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Ports.NetPositionRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Presenters.BatchPresenterInterface;
import net.tanguydev.settlementservice.Domain.Responses.BatchResponse;
import net.tanguydev.settlementservice.Domain.UseCases.CloseBatchUseCaseInterface;
import net.tanguydev.settlementservice.Domain.UseCases.OpenBatchUseCaseInterface;
import net.tanguydev.settlementservice.Domain.Validations.Exception.BatchNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements/batches")
public class SettlementController {

    private final SettlementBatchRepositoryInterface batchRepository;
    private final SettlementEntryRepositoryInterface entryRepository;
    private final NetPositionRepositoryInterface positionRepository;
    private final BatchPresenterInterface presenter;
    private final OpenBatchUseCaseInterface openBatchUseCase;
    private final CloseBatchUseCaseInterface closeBatchUseCase;

    public SettlementController(SettlementBatchRepositoryInterface batchRepository,
                                SettlementEntryRepositoryInterface entryRepository,
                                NetPositionRepositoryInterface positionRepository,
                                BatchPresenterInterface presenter,
                                OpenBatchUseCaseInterface openBatchUseCase,
                                CloseBatchUseCaseInterface closeBatchUseCase) {
        this.batchRepository = batchRepository;
        this.entryRepository = entryRepository;
        this.positionRepository = positionRepository;
        this.presenter = presenter;
        this.openBatchUseCase = openBatchUseCase;
        this.closeBatchUseCase = closeBatchUseCase;
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

    @PostMapping("/open")
    public ResponseEntity<BatchResponse> openBatch(
            @RequestParam(defaultValue = "HOURLY") SettlementCycle cycle,
            @RequestParam(defaultValue = "XAF") String currency) {
        DomainSettlementBatch batch = openBatchUseCase.execute(cycle, currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(presenter.present(batch));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Void> closeBatch(@PathVariable UUID id) {
        closeBatchUseCase.execute(id);
        return ResponseEntity.ok().build();
    }

    private DomainSettlementBatch enrichBatch(DomainSettlementBatch batch) {
        batch.setEntries(entryRepository.findByBatchId(batch.getId()));
        batch.setPositions(positionRepository.findByBatchId(batch.getId()));
        return batch;
    }
}
