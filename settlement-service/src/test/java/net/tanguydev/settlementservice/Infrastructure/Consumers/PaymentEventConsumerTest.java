package net.tanguydev.settlementservice.Infrastructure.Consumers;

import net.tanguydev.settlementservice.Domain.UseCases.ProcessPaymentSettlementCommand;
import net.tanguydev.settlementservice.Domain.UseCases.ProcessPaymentSettlementUseCaseInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock private ProcessPaymentSettlementUseCaseInterface processSettlement;

    private PaymentEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PaymentEventConsumer(processSettlement);
    }

    private Map<String, Object> paymentCompletedEvent() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "payment.completed");
        event.put("paymentId", UUID.randomUUID().toString());
        event.put("paymentReference", "PAY-REF-001");
        event.put("senderWalletId", UUID.randomUUID().toString());
        event.put("receiverWalletId", UUID.randomUUID().toString());
        event.put("amount", "5000.00");
        event.put("feeAmount", "50.00");
        event.put("currency", "XAF");
        return event;
    }

    @Test
    void consume_paymentCompleted_callsUseCase() {
        Map<String, Object> event = paymentCompletedEvent();

        consumer.consume(event);

        ArgumentCaptor<ProcessPaymentSettlementCommand> captor =
                ArgumentCaptor.forClass(ProcessPaymentSettlementCommand.class);
        verify(processSettlement).execute(captor.capture());

        ProcessPaymentSettlementCommand cmd = captor.getValue();
        assertThat(cmd.getPaymentReference()).isEqualTo("PAY-REF-001");
        assertThat(cmd.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(cmd.getFeeAmount()).isEqualByComparingTo("50.00");
        assertThat(cmd.getCurrency()).isEqualTo("XAF");
    }

    @Test
    void consume_paymentInitiated_ignored() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "payment.initiated");

        consumer.consume(event);

        verify(processSettlement, never()).execute(any());
    }

    @Test
    void consume_unknownEventType_ignored() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "payment.failed");

        consumer.consume(event);

        verify(processSettlement, never()).execute(any());
    }

    @Test
    void consume_nullFeeAmount_defaultsToZero() {
        Map<String, Object> event = paymentCompletedEvent();
        event.remove("feeAmount");

        consumer.consume(event);

        ArgumentCaptor<ProcessPaymentSettlementCommand> captor =
                ArgumentCaptor.forClass(ProcessPaymentSettlementCommand.class);
        verify(processSettlement).execute(captor.capture());

        assertThat(captor.getValue().getFeeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
