package net.tanguydev.settlementservice.Infrastructure.Controllers;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
import net.tanguydev.settlementservice.Domain.Presenters.SettlementPresenterInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Responses.SettlementResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementRepositoryInterface settlementRepository;
    private final SettlementPresenterInterface presenter;

    public SettlementController(SettlementRepositoryInterface settlementRepository,
                                SettlementPresenterInterface presenter) {
        this.settlementRepository = settlementRepository;
        this.presenter = presenter;
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> getAll() {
        List<SettlementResponse> responses = settlementRepository.findAll().stream()
                .map(presenter::present)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SettlementResponse> getById(@PathVariable UUID id) {
        return settlementRepository.findById(id)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
