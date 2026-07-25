package net.tanguydev.walletservice.Infrastructure.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Presenters.WalletPresenterInterface;
import net.tanguydev.walletservice.Domain.Responses.WalletResponse;
import net.tanguydev.walletservice.Domain.UseCases.FindWalletByCustomerIdUseCaseInterface;
import net.tanguydev.walletservice.Domain.UseCases.FindWalletByIdUseCaseInterface;
import net.tanguydev.walletservice.Domain.UseCases.GetWalletHistoryUseCaseInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Wallets")
@RestController
@RequestMapping("/api/v1/wallets")
@SecurityRequirement(name = "BearerAuth")
public class WalletQueryController {

    private final FindWalletByIdUseCaseInterface findById;
    private final FindWalletByCustomerIdUseCaseInterface findByCustomerId;
    private final GetWalletHistoryUseCaseInterface getHistory;
    private final WalletPresenterInterface presenter;

    public WalletQueryController(FindWalletByIdUseCaseInterface findById,
                                 FindWalletByCustomerIdUseCaseInterface findByCustomerId,
                                 GetWalletHistoryUseCaseInterface getHistory,
                                 WalletPresenterInterface presenter) {
        this.findById = findById;
        this.findByCustomerId = findByCustomerId;
        this.getHistory = getHistory;
        this.presenter = presenter;
    }

    @Operation(summary = "Get a wallet by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallet found"),
                    @ApiResponse(responseCode = "404", description = "Wallet not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> show(@PathVariable UUID id) {
        return findById.execute(id)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get wallet by customer ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallet found"),
                    @ApiResponse(responseCode = "404", description = "Wallet not found")
            })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<WalletResponse> showByCustomer(@PathVariable UUID customerId) {
        return findByCustomerId.execute(customerId)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get wallet event history",
            description = "Returns the full append-only event log. Replay these events to reconstruct the wallet state at any point in time.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event history"),
                    @ApiResponse(responseCode = "404", description = "Wallet not found")
            })
    @GetMapping("/{id}/history")
    public ResponseEntity<List<WalletEventEntry>> history(@PathVariable UUID id) {
        List<WalletEventEntry> events = getHistory.execute(id);
        return ResponseEntity.ok(events);
    }
}
