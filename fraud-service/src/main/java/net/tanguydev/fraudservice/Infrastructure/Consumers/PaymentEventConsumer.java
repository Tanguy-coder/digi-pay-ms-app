package net.tanguydev.fraudservice.Infrastructure.Consumers;

import net.tanguydev.fraudservice.Domain.UseCases.AnalyzePaymentCommand;
import net.tanguydev.fraudservice.Domain.UseCases.AnalyzePaymentUseCaseInterface;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Écoute payment-events et déclenche l'analyse fraude sur "payment.initiated".
 */
@Component
public class PaymentEventConsumer {

    private final AnalyzePaymentUseCaseInterface analyzePayment;

    public PaymentEventConsumer(AnalyzePaymentUseCaseInterface analyzePayment) {
        this.analyzePayment = analyzePayment;
    }

    @KafkaListener(topics = "payment-events", groupId = "fraud-group")
    @Transactional
    public void consume(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");
        if (!"payment.initiated".equals(eventType)) return;

        AnalyzePaymentCommand command = new AnalyzePaymentCommand();
        command.setPaymentId(UUID.fromString((String) message.get("paymentId")));
        command.setSenderWalletId(UUID.fromString((String) message.get("senderWalletId")));
        command.setAmount(new BigDecimal(message.get("amount").toString()));
        command.setCurrency((String) message.get("currency"));
        // customerId sera enrichi quand le PaymentEvent l'exposera
        command.setCustomerId(command.getSenderWalletId());

        analyzePayment.execute(command);
    }
}
