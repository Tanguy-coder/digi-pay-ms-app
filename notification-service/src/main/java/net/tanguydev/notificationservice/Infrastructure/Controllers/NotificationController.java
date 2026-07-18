package net.tanguydev.notificationservice.Infrastructure.Controllers;

import net.tanguydev.notificationservice.Domain.Ports.NotificationRepositoryInterface;
import net.tanguydev.notificationservice.Domain.Presenters.NotificationPresenterInterface;
import net.tanguydev.notificationservice.Domain.Responses.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepositoryInterface repository;
    private final NotificationPresenterInterface presenter;

    public NotificationController(NotificationRepositoryInterface repository, NotificationPresenterInterface presenter) {
        this.repository = repository;
        this.presenter = presenter;
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<NotificationResponse>> showByWallet(@PathVariable UUID walletId) {
        return ResponseEntity.ok(presenter.present(repository.findByWalletId(walletId)));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<NotificationResponse>> showByPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(presenter.present(repository.findByPaymentId(paymentId)));
    }
}
