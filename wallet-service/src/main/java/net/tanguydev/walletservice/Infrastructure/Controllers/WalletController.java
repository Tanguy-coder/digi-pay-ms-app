package net.tanguydev.walletservice.Infrastructure.Controllers;

import jakarta.validation.Valid;
import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Presenters.WalletPresenterInterface;
import net.tanguydev.walletservice.Domain.Responses.WalletResponse;
import net.tanguydev.walletservice.Domain.UseCases.*;
import net.tanguydev.walletservice.Infrastructure.Mappers.WalletMapper;
import net.tanguydev.walletservice.Infrastructure.Requests.WalletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final CreateWalletUseCaseInterface create;
    private final FindWalletByIdUseCaseInterface findById;
    private final FindWalletByCustomerIdUseCaseInterface findByCustomerId;
    private final CreditWalletUseCaseInterface credit;
    private final DebitWalletUseCaseInterface debit;
    private final FreezeAmountUseCaseInterface freeze;
    private final GetWalletHistoryUseCaseInterface getHistory;
    private final WalletPresenterInterface presenter;
    private final WalletMapper mapper;

    public WalletController(CreateWalletUseCaseInterface create,
                            FindWalletByIdUseCaseInterface findById,
                            FindWalletByCustomerIdUseCaseInterface findByCustomerId,
                            CreditWalletUseCaseInterface credit,
                            DebitWalletUseCaseInterface debit,
                            FreezeAmountUseCaseInterface freeze,
                            GetWalletHistoryUseCaseInterface getHistory,
                            WalletPresenterInterface presenter,
                            WalletMapper mapper) {
        this.create = create;
        this.findById = findById;
        this.findByCustomerId = findByCustomerId;
        this.credit = credit;
        this.debit = debit;
        this.freeze = freeze;
        this.getHistory = getHistory;
        this.presenter = presenter;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> store(@Valid @RequestBody WalletRequest request) {
        DomainWallet domainWallet = mapper.requestToDomain(request);
        DomainWallet created = create.execute(domainWallet);
        return ResponseEntity.status(201).body(presenter.present(created));
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

    @PostMapping("/{id}/credit")
    public ResponseEntity<WalletResponse> credit(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        DomainWallet credited = credit.execute(id, amount);
        return ResponseEntity.ok(presenter.present(credited));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<WalletResponse> debit(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        DomainWallet debited = debit.execute(id, amount);
        return ResponseEntity.ok(presenter.present(debited));
    }

    @PostMapping("/{id}/freeze")
    public ResponseEntity<WalletResponse> freeze(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        DomainWallet frozen = freeze.execute(id, amount);
        return ResponseEntity.ok(presenter.present(frozen));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<WalletEventEntry>> history(@PathVariable UUID id) {
        List<WalletEventEntry> events = getHistory.execute(id);
        return ResponseEntity.ok(events);
    }
}
