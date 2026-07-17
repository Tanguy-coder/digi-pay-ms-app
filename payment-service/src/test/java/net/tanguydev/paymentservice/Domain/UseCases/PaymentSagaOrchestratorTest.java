package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Commands.WalletCommand;
import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Entities.DomainSagaStep;
import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Enums.PaymentType;
import net.tanguydev.paymentservice.Domain.Enums.SagaStepName;
import net.tanguydev.paymentservice.Domain.Enums.SagaStepStatus;
import net.tanguydev.paymentservice.Domain.Events.PaymentEvent;
import net.tanguydev.paymentservice.Domain.Gateways.SagaStepRepositoryInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentEventPublisherInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentServiceInterface;
import net.tanguydev.paymentservice.Domain.Ports.WalletCommandPublisherInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSagaOrchestratorTest {

    @Mock private PaymentServiceInterface paymentService;
    @Mock private SagaStepRepositoryInterface sagaStepRepository;
    @Mock private WalletCommandPublisherInterface commandPublisher;
    @Mock private PaymentEventPublisherInterface eventPublisher;

    private PaymentSagaOrchestrator orchestrator;

    private static final UUID PAYMENT_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SENDER_ID   = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID RECEIVER_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @BeforeEach
    void setUp() {
        orchestrator = new PaymentSagaOrchestrator(paymentService, sagaStepRepository, commandPublisher, eventPublisher);
    }

    @Test
    void startSaga_shouldCreate4StepsAndSendDebitCommand() {
        DomainPayment payment = buildPayment();

        orchestrator.startSaga(payment);

        verify(sagaStepRepository, times(4)).save(any(DomainSagaStep.class));

        ArgumentCaptor<WalletCommand> cmdCaptor = ArgumentCaptor.forClass(WalletCommand.class);
        verify(commandPublisher).publish(cmdCaptor.capture());
        WalletCommand cmd = cmdCaptor.getValue();
        assertEquals("DEBIT", cmd.getCommandType());
        assertEquals(SENDER_ID, cmd.getWalletId());
        assertEquals(new BigDecimal("5000"), cmd.getAmount());
    }

    @Test
    void onDebitSuccess_shouldSendCreditCommandAndSetProcessing() {
        DomainPayment payment = buildPayment();
        DomainSagaStep debitStep = buildStep(SagaStepName.DEBIT_SENDER);
        DomainSagaStep creditStep = buildStep(SagaStepName.CREDIT_RECEIVER);

        when(sagaStepRepository.findByPaymentId(PAYMENT_ID))
                .thenReturn(List.of(debitStep, creditStep));
        when(paymentService.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentService.save(any())).thenReturn(payment);

        orchestrator.onDebitSuccess(PAYMENT_ID);

        ArgumentCaptor<DomainPayment> paymentCaptor = ArgumentCaptor.forClass(DomainPayment.class);
        verify(paymentService).save(paymentCaptor.capture());
        assertEquals(PaymentStatus.PROCESSING, paymentCaptor.getValue().getStatus());

        ArgumentCaptor<WalletCommand> cmdCaptor = ArgumentCaptor.forClass(WalletCommand.class);
        verify(commandPublisher).publish(cmdCaptor.capture());
        assertEquals("CREDIT", cmdCaptor.getValue().getCommandType());
        assertEquals(RECEIVER_ID, cmdCaptor.getValue().getWalletId());
    }

    @Test
    void onDebitFailure_shouldMarkPaymentFailed() {
        DomainPayment payment = buildPayment();
        DomainSagaStep debitStep = buildStep(SagaStepName.DEBIT_SENDER);

        when(sagaStepRepository.findByPaymentId(PAYMENT_ID)).thenReturn(List.of(debitStep));
        when(paymentService.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentService.save(any())).thenReturn(payment);

        orchestrator.onDebitFailure(PAYMENT_ID, "Solde insuffisant");

        ArgumentCaptor<DomainPayment> captor = ArgumentCaptor.forClass(DomainPayment.class);
        verify(paymentService).save(captor.capture());
        assertEquals(PaymentStatus.FAILED, captor.getValue().getStatus());
        assertEquals("Solde insuffisant", captor.getValue().getFailureReason());

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("payment.failed", eventCaptor.getValue().getEventType());
    }

    @Test
    void onCreditSuccess_shouldMarkCompletedAndPublishEvent() {
        DomainPayment payment = buildPayment();
        DomainSagaStep creditStep  = buildStep(SagaStepName.CREDIT_RECEIVER);
        DomainSagaStep notifyStep  = buildStep(SagaStepName.NOTIFY);
        DomainSagaStep settleStep  = buildStep(SagaStepName.SETTLE);

        when(sagaStepRepository.findByPaymentId(PAYMENT_ID))
                .thenReturn(List.of(creditStep, notifyStep, settleStep));
        when(paymentService.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentService.save(any())).thenReturn(payment);

        orchestrator.onCreditSuccess(PAYMENT_ID);

        ArgumentCaptor<DomainPayment> captor = ArgumentCaptor.forClass(DomainPayment.class);
        verify(paymentService).save(captor.capture());
        assertEquals(PaymentStatus.COMPLETED, captor.getValue().getStatus());

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("payment.completed", eventCaptor.getValue().getEventType());
    }

    @Test
    void onCreditFailure_shouldSendCompensationCommand() {
        DomainPayment payment = buildPayment();
        DomainSagaStep creditStep = buildStep(SagaStepName.CREDIT_RECEIVER);

        when(sagaStepRepository.findByPaymentId(PAYMENT_ID)).thenReturn(List.of(creditStep));
        when(paymentService.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        orchestrator.onCreditFailure(PAYMENT_ID, "Wallet inactif");

        ArgumentCaptor<WalletCommand> cmdCaptor = ArgumentCaptor.forClass(WalletCommand.class);
        verify(commandPublisher).publish(cmdCaptor.capture());
        assertEquals("COMPENSATE_DEBIT", cmdCaptor.getValue().getCommandType());
        assertEquals(SENDER_ID, cmdCaptor.getValue().getWalletId());
        assertEquals(new BigDecimal("5000"), cmdCaptor.getValue().getAmount());
    }

    @Test
    void onCompensationCompleted_shouldMarkReversedAndPublishEvent() {
        DomainPayment payment = buildPayment();
        DomainSagaStep debitStep = buildStep(SagaStepName.DEBIT_SENDER);

        when(sagaStepRepository.findByPaymentId(PAYMENT_ID)).thenReturn(List.of(debitStep));
        when(paymentService.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentService.save(any())).thenReturn(payment);

        orchestrator.onCompensationCompleted(PAYMENT_ID);

        ArgumentCaptor<DomainPayment> captor = ArgumentCaptor.forClass(DomainPayment.class);
        verify(paymentService).save(captor.capture());
        assertEquals(PaymentStatus.REVERSED, captor.getValue().getStatus());

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("payment.reversed", eventCaptor.getValue().getEventType());
    }

    @Test
    void onCompensationFailed_shouldMarkFailedWithPrefix() {
        DomainPayment payment = buildPayment();
        when(paymentService.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentService.save(any())).thenReturn(payment);

        orchestrator.onCompensationFailed(PAYMENT_ID, "Timeout");

        ArgumentCaptor<DomainPayment> captor = ArgumentCaptor.forClass(DomainPayment.class);
        verify(paymentService).save(captor.capture());
        assertEquals(PaymentStatus.FAILED, captor.getValue().getStatus());
        assertTrue(captor.getValue().getFailureReason().startsWith("COMPENSATION_FAILED:"));

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("payment.compensation_failed", eventCaptor.getValue().getEventType());
    }

    private DomainPayment buildPayment() {
        DomainPayment p = new DomainPayment();
        p.setId(PAYMENT_ID);
        p.setSenderWalletId(SENDER_ID);
        p.setReceiverWalletId(RECEIVER_ID);
        p.setAmount(new BigDecimal("5000"));
        p.setFeeAmount(BigDecimal.ZERO);
        p.setCurrency("XOF");
        p.setType(PaymentType.P2P);
        p.setStatus(PaymentStatus.INITIATED);
        p.setPaymentReference("PAY-20260717-12345");
        p.setIdempotencyKey("KEY-001");
        return p;
    }

    private DomainSagaStep buildStep(SagaStepName name) {
        DomainSagaStep step = new DomainSagaStep();
        step.setPaymentId(PAYMENT_ID);
        step.setStepName(name);
        step.setStepStatus(SagaStepStatus.PENDING);
        step.setRetryCount(0);
        step.setMaxRetries(3);
        return step;
    }
}
