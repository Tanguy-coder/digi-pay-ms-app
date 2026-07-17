package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Enums.PaymentType;
import net.tanguydev.paymentservice.Domain.Events.PaymentEvent;
import net.tanguydev.paymentservice.Domain.Ports.IdempotencyStoreInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentEventPublisherInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentServiceInterface;
import net.tanguydev.paymentservice.Domain.Validations.Exception.DuplicatePaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitiatePaymentUseCaseTest {

    @Mock private PaymentServiceInterface paymentService;
    @Mock private PaymentEventPublisherInterface eventPublisher;
    @Mock private IdempotencyStoreInterface idempotencyStore;
    @Mock private PaymentSagaOrchestratorInterface sagaOrchestrator;

    private InitiatePaymentUseCase useCase;

    private static final UUID PAYMENT_ID   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SENDER_ID    = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID RECEIVER_ID  = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @BeforeEach
    void setUp() {
        useCase = new InitiatePaymentUseCase(paymentService, eventPublisher, idempotencyStore, sagaOrchestrator);
    }

    @Test
    void execute_shouldSavePublishEventAndStartSaga() {
        DomainPayment input = buildInput();

        DomainPayment saved = buildSaved();
        when(idempotencyStore.exists("KEY-001")).thenReturn(false);
        when(paymentService.save(any(DomainPayment.class))).thenReturn(saved);

        DomainPayment result = useCase.execute(input);

        assertNotNull(result);
        assertEquals(PAYMENT_ID, result.getId());
        assertEquals(PaymentStatus.INITIATED, result.getStatus());

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("payment.initiated", eventCaptor.getValue().getEventType());
        assertEquals(PAYMENT_ID, eventCaptor.getValue().getPaymentId());

        verify(sagaOrchestrator).startSaga(saved);
    }

    @Test
    void execute_shouldSetDefaults_whenOptionalFieldsNull() {
        DomainPayment input = buildInput();
        input.setFeeAmount(null);
        input.setExchangeRate(null);
        input.setStatus(null);
        input.setPaymentReference(null);

        when(idempotencyStore.exists(any())).thenReturn(false);
        when(paymentService.save(any(DomainPayment.class))).thenAnswer(inv -> {
            DomainPayment p = inv.getArgument(0);
            p.setId(PAYMENT_ID);
            return p;
        });

        DomainPayment result = useCase.execute(input);

        assertEquals(BigDecimal.ZERO, result.getFeeAmount());
        assertEquals(BigDecimal.ONE, result.getExchangeRate());
        assertEquals(PaymentStatus.INITIATED, result.getStatus());
        assertNotNull(result.getPaymentReference());
        assertTrue(result.getPaymentReference().startsWith("PAY-"));
    }

    @Test
    void execute_shouldThrowDuplicatePaymentException_whenKeyExists() {
        DomainPayment input = buildInput();
        when(idempotencyStore.exists("KEY-001")).thenReturn(true);

        assertThrows(DuplicatePaymentException.class, () -> useCase.execute(input));

        verify(paymentService, never()).save(any());
        verify(sagaOrchestrator, never()).startSaga(any());
    }

    private DomainPayment buildInput() {
        DomainPayment p = new DomainPayment();
        p.setSenderWalletId(SENDER_ID);
        p.setReceiverWalletId(RECEIVER_ID);
        p.setAmount(new BigDecimal("5000"));
        p.setCurrency("XOF");
        p.setType(PaymentType.P2P);
        p.setIdempotencyKey("KEY-001");
        return p;
    }

    private DomainPayment buildSaved() {
        DomainPayment p = buildInput();
        p.setId(PAYMENT_ID);
        p.setStatus(PaymentStatus.INITIATED);
        p.setFeeAmount(BigDecimal.ZERO);
        p.setExchangeRate(BigDecimal.ONE);
        p.setPaymentReference("PAY-20260717-12345");
        return p;
    }
}
