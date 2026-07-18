package net.tanguydev.notificationservice.Domain.UseCases;

import net.tanguydev.notificationservice.Domain.Entities.DomainNotification;
import net.tanguydev.notificationservice.Domain.Enums.NotificationStatus;
import net.tanguydev.notificationservice.Domain.Ports.NotificationRepositoryInterface;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SendNotificationUseCase implements SendNotificationUseCaseInterface {

    private final NotificationRepositoryInterface repository;

    public SendNotificationUseCase(NotificationRepositoryInterface repository) {
        this.repository = repository;
    }

    @Override
    public void execute(SendNotificationCommand command) {
        DomainNotification notification = new DomainNotification();
        notification.setId(UUID.randomUUID());
        notification.setWalletId(command.getWalletId());
        notification.setPaymentId(command.getPaymentId());
        notification.setType(command.getType());
        notification.setAmount(command.getAmount());
        notification.setCurrency(command.getCurrency());
        notification.setMessage(buildMessage(command));
        notification.setStatus(NotificationStatus.SENT);
        notification.setCreatedAt(OffsetDateTime.now());

        repository.save(notification);
    }

    private String buildMessage(SendNotificationCommand command) {
        return switch (command.getType()) {
            case PAYMENT_INITIATED -> "Payment of %s %s initiated.".formatted(command.getAmount(), command.getCurrency());
            case PAYMENT_COMPLETED -> "Payment of %s %s completed successfully.".formatted(command.getAmount(), command.getCurrency());
            case PAYMENT_FAILED    -> "Payment of %s %s failed.".formatted(command.getAmount(), command.getCurrency());
            case FRAUD_BLOCKED     -> "Payment of %s %s was blocked due to fraud detection.".formatted(command.getAmount(), command.getCurrency());
            case FRAUD_REVIEW      -> "Payment of %s %s is under fraud review.".formatted(command.getAmount(), command.getCurrency());
        };
    }
}
