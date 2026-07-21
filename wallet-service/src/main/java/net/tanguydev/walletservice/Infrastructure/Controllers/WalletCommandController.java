package net.tanguydev.walletservice.Infrastructure.Controllers;

import jakarta.validation.Valid;
import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Presenters.WalletPresenterInterface;
import net.tanguydev.walletservice.Domain.Responses.WalletResponse;
import net.tanguydev.walletservice.Domain.UseCases.CreateWalletUseCaseInterface;
import net.tanguydev.walletservice.Domain.UseCases.CreditWalletUseCaseInterface;
import net.tanguydev.walletservice.Domain.UseCases.DebitWalletUseCaseInterface;
import net.tanguydev.walletservice.Domain.UseCases.FreezeAmountUseCaseInterface;
import net.tanguydev.walletservice.Infrastructure.Mappers.WalletMapper;
import net.tanguydev.walletservice.Infrastructure.Requests.WalletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletCommandController {

    private final CreateWalletUseCaseInterface create;
    private final CreditWalletUseCaseInterface credit;
    private final DebitWalletUseCaseInterface debit;
    private final FreezeAmountUseCaseInterface freeze;
    private final WalletPresenterInterface presenter;
    private final WalletMapper mapper;

    public WalletCommandController(CreateWalletUseCaseInterface create,
                                   CreditWalletUseCaseInterface credit,
                                   DebitWalletUseCaseInterface debit,
                                   FreezeAmountUseCaseInterface freeze,
                                   WalletPresenterInterface presenter,
                                   WalletMapper mapper) {
        this.create = create;
        this.credit = credit;
        this.debit = debit;
        this.freeze = freeze;
        this.presenter = presenter;
        this.mapper = mapper;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<WalletResponse> store(@Valid @RequestBody WalletRequest request) {
        DomainWallet domainWallet = mapper.requestToDomain(request);
        DomainWallet created = create.execute(domainWallet);
        return ResponseEntity.status(201).body(presenter.present(created));
    }

    @PostMapping("/{id}/credit")
    @Transactional
    public ResponseEntity<WalletResponse> credit(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        DomainWallet credited = credit.execute(id, amount);
        return ResponseEntity.ok(presenter.present(credited));
    }

    @PostMapping("/{id}/debit")
    @Transactional
    public ResponseEntity<WalletResponse> debit(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        DomainWallet debited = debit.execute(id, amount);
        return ResponseEntity.ok(presenter.present(debited));
    }

    @PostMapping("/{id}/freeze")
    @Transactional
    public ResponseEntity<WalletResponse> freeze(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        DomainWallet frozen = freeze.execute(id, amount);
        return ResponseEntity.ok(presenter.present(frozen));
    }
}
