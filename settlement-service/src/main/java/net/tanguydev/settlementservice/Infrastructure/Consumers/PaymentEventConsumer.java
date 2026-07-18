package net.tanguydev.settlementservice.Infrastructure.Consumers;

import net.tanguydev.settlementservice.Domain.UseCases.ProcessPaymentSettlementCommand;
import net.tanguydev.settlementservice.Domain.UseCases.ProcessPaymentSettlementUseCaseInterface;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private final ProcessPaymentSettlementUseCaseInterface processSettlement;

    public PaymentEventConsumer(ProcessPaymentSettlementUseCaseInterface processSettlement) {
        this.processSettlement = processSettlement;
    }

    @KafkaListener(topics = "payment-events", groupId = "settlement-group")
    public void consume(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        if (!"payment.completed".equals(eventType)) return;

        ProcessPaymentSettlementCommand command = new ProcessPaymentSettlementCommand();
        command.setPaymentId(UUID.fromString((String) message.get("paymentId")));
        command.setPaymentReference((String) message.get("paymentReference"));
        command.setSenderWalletId(UUID.fromString((String) message.get("senderWalletId")));
        command.setReceiverWalletId(UUID.fromString((String) message.get("receiverWalletId")));
        command.setAmount(new BigDecimal(message.get("amount").toString()));

        Object fee = message.get("feeAmount");
        command.setFeeAmount(fee != null ? new BigDecimal(fee.toString()) : BigDecimal.ZERO);

        command.setCurrency((String) message.get("currency"));

        processSettlement.execute(command);
    }
}
