package net.tanguydev.walletservice.Infrastructure.Controllers;

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

@RestController
@RequestMapping("/api/v1/wallets")
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

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> show(@PathVariable UUID id) {
        return findById.execute(id)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<WalletResponse> showByCustomer(@PathVariable UUID customerId) {
        return findByCustomerId.execute(customerId)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<WalletEventEntry>> history(@PathVariable UUID id) {
        List<WalletEventEntry> events = getHistory.execute(id);
        return ResponseEntity.ok(events);
    }
}
