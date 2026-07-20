package net.tanguydev.settlementservice.Infrastructure.Consumers;

import net.tanguydev.settlementservice.Domain.UseCases.CaptureEntryCommand;
import net.tanguydev.settlementservice.Domain.UseCases.CaptureEntryUseCaseInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final CaptureEntryUseCaseInterface captureEntry;

    public PaymentEventConsumer(CaptureEntryUseCaseInterface captureEntry) {
        this.captureEntry = captureEntry;
    }

    @KafkaListener(topics = "payment-events", groupId = "settlement-group")
    public void consume(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        if (!"payment.completed".equals(eventType)) return;

        CaptureEntryCommand command = new CaptureEntryCommand();
        command.setPaymentId(UUID.fromString((String) message.get("paymentId")));
        command.setPaymentReference((String) message.get("paymentReference"));
        command.setSenderWalletId(UUID.fromString((String) message.get("senderWalletId")));
        command.setReceiverWalletId(UUID.fromString((String) message.get("receiverWalletId")));
        command.setAmount(new BigDecimal(message.get("amount").toString()));

        Object fee = message.get("feeAmount");
        command.setFeeAmount(fee != null ? new BigDecimal(fee.toString()) : BigDecimal.ZERO);

        command.setCurrency((String) message.get("currency"));

        try {
            captureEntry.execute(command);
        } catch (Exception e) {
            log.error("Failed to capture settlement entry for payment {}: {}", command.getPaymentId(), e.getMessage());
        }
    }
}
