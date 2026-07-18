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
public class PaymentEventConsumer {

    private final SendNotificationUseCaseInterface sendNotification;

    public PaymentEventConsumer(SendNotificationUseCaseInterface sendNotification) {
        this.sendNotification = sendNotification;
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-payment-group")
    public void consume(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");

        NotificationType type = switch (eventType) {
            case "payment.initiated"  -> NotificationType.PAYMENT_INITIATED;
            case "payment.completed"  -> NotificationType.PAYMENT_COMPLETED;
            case "payment.failed"     -> NotificationType.PAYMENT_FAILED;
            default -> null;
        };

        if (type == null) return;

        SendNotificationCommand command = new SendNotificationCommand();
        command.setPaymentId(UUID.fromString((String) message.get("paymentId")));
        command.setWalletId(UUID.fromString((String) message.get("senderWalletId")));
        command.setType(type);
        command.setAmount(new BigDecimal(message.get("amount").toString()));
        command.setCurrency((String) message.get("currency"));

        sendNotification.execute(command);
    }
}
