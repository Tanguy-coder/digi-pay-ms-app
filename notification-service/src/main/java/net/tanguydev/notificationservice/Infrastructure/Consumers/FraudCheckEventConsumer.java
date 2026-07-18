package net.tanguydev.notificationservice.Infrastructure.Consumers;

import net.tanguydev.notificationservice.Domain.Enums.NotificationType;
import net.tanguydev.notificationservice.Domain.UseCases.SendNotificationCommand;
import net.tanguydev.notificationservice.Domain.UseCases.SendNotificationUseCaseInterface;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class FraudCheckEventConsumer {

    private final SendNotificationUseCaseInterface sendNotification;

    public FraudCheckEventConsumer(SendNotificationUseCaseInterface sendNotification) {
        this.sendNotification = sendNotification;
    }

    @KafkaListener(topics = "fraud-check-events", groupId = "notification-fraud-group")
    public void consume(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");

        NotificationType type = switch (eventType) {
            case "fraud.blocked" -> NotificationType.FRAUD_BLOCKED;
            case "fraud.review"  -> NotificationType.FRAUD_REVIEW;
            default -> null;
        };

        if (type == null) return;

        SendNotificationCommand command = new SendNotificationCommand();
        command.setPaymentId(UUID.fromString((String) message.get("paymentId")));
        command.setWalletId(UUID.fromString((String) message.get("customerId")));
        command.setType(type);
        // amount/currency not in fraud event — set defaults
        command.setAmount(BigDecimal.ZERO);
        command.setCurrency("N/A");

        sendNotification.execute(command);
    }
}
