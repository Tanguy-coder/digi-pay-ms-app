package net.tanguydev.notificationservice.Infrastructure.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.tanguydev.notificationservice.Domain.Ports.NotificationRepositoryInterface;
import net.tanguydev.notificationservice.Domain.Presenters.NotificationPresenterInterface;
import net.tanguydev.notificationservice.Domain.Responses.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notifications")
@RestController
@RequestMapping("/api/v1/notifications")
@SecurityRequirement(name = "BearerAuth")
public class NotificationQueryController {

    private final NotificationRepositoryInterface repository;
    private final NotificationPresenterInterface presenter;

    public NotificationQueryController(NotificationRepositoryInterface repository,
                                       NotificationPresenterInterface presenter) {
        this.repository = repository;
        this.presenter = presenter;
    }

    @Operation(summary = "List notifications by wallet",
            responses = @ApiResponse(responseCode = "200", description = "Notification history for the wallet"))
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<NotificationResponse>> showByWallet(@PathVariable UUID walletId) {
        return ResponseEntity.ok(presenter.present(repository.findByWalletId(walletId)));
    }

    @Operation(summary = "List notifications by payment",
            responses = @ApiResponse(responseCode = "200", description = "Notification history for the payment"))
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<NotificationResponse>> showByPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(presenter.present(repository.findByPaymentId(paymentId)));
    }
}
