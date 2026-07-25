package net.tanguydev.walletservice.Infrastructure.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Wallets")
@RestController
@RequestMapping("/api/v1/wallets")
@SecurityRequirement(name = "BearerAuth")
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

    @Operation(summary = "Create a wallet",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Wallet created"),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            })
    @PostMapping
    @Transactional
    public ResponseEntity<WalletResponse> store(@Valid @RequestBody WalletRequest request) {
        DomainWallet domainWallet = mapper.requestToDomain(request);
        DomainWallet created = create.execute(domainWallet);
        return ResponseEntity.status(201).body(presenter.present(created));
    }

    @Operation(summary = "Credit a wallet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallet credited"),
                    @ApiResponse(responseCode = "404", description = "Wallet not found")
            })
    @PostMapping("/{id}/credit")
    @Transactional
    public ResponseEntity<WalletResponse> credit(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        DomainWallet credited = credit.execute(id, amount);
        return ResponseEntity.ok(presenter.present(credited));
    }

    @Operation(summary = "Debit a wallet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Wallet debited"),
                    @ApiResponse(responseCode = "400", description = "Insufficient balance"),
                    @ApiResponse(responseCode = "404", description = "Wallet not found")
            })
    @PostMapping("/{id}/debit")
    @Transactional
    public ResponseEntity<WalletResponse> debit(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        DomainWallet debited = debit.execute(id, amount);
        return ResponseEntity.ok(presenter.present(debited));
    }

    @Operation(summary = "Freeze funds in a wallet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Funds frozen"),
                    @ApiResponse(responseCode = "400", description = "Insufficient available balance"),
                    @ApiResponse(responseCode = "404", description = "Wallet not found")
            })
    @PostMapping("/{id}/freeze")
    @Transactional
    public ResponseEntity<WalletResponse> freeze(@PathVariable UUID id, @RequestParam BigDecimal amount) {
        DomainWallet frozen = freeze.execute(id, amount);
        return ResponseEntity.ok(presenter.present(frozen));
    }
}
