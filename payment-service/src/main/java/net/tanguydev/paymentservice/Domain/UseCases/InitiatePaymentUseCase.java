package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Events.PaymentEvent;
import net.tanguydev.paymentservice.Domain.Ports.IdempotencyStoreInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentEventPublisherInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentServiceInterface;
import net.tanguydev.paymentservice.Domain.Entities.DomainIdempotencyKey;
import net.tanguydev.paymentservice.Domain.Validations.DomainPaymentValidator;
import net.tanguydev.paymentservice.Domain.Validations.Exception.DuplicatePaymentException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class InitiatePaymentUseCase implements InitiatePaymentUseCaseInterface {

    private final PaymentServiceInterface paymentService;
    private final PaymentEventPublisherInterface eventPublisher;
    private final IdempotencyStoreInterface idempotencyStore;
    private final PaymentSagaOrchestratorInterface sagaOrchestrator;
    private final DomainPaymentValidator validator = new DomainPaymentValidator();

    public InitiatePaymentUseCase(PaymentServiceInterface paymentService,
                                  PaymentEventPublisherInterface eventPublisher,
                                  IdempotencyStoreInterface idempotencyStore,
                                  PaymentSagaOrchestratorInterface sagaOrchestrator) {
        this.paymentService = paymentService;
        this.eventPublisher = eventPublisher;
        this.idempotencyStore = idempotencyStore;
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @Override
    public DomainPayment execute(DomainPayment payment) {
        if (idempotencyStore.exists(payment.getIdempotencyKey())) {
            throw new DuplicatePaymentException(payment.getIdempotencyKey());
        }

        if (payment.getFeeAmount() == null) payment.setFeeAmount(BigDecimal.ZERO);
        if (payment.getExchangeRate() == null) payment.setExchangeRate(BigDecimal.ONE);
        if (payment.getStatus() == null) payment.setStatus(PaymentStatus.INITIATED);
        if (payment.getPaymentReference() == null) payment.setPaymentReference(generateReference());
        payment.setInitiatedAt(OffsetDateTime.now());

        validator.validate(payment);

        DomainPayment saved = paymentService.save(payment);

        DomainIdempotencyKey idempotencyEntry = new DomainIdempotencyKey();
        idempotencyEntry.setKey(saved.getIdempotencyKey());
        idempotencyEntry.setResponseStatus(201);
        idempotencyEntry.setExpiresAt(OffsetDateTime.now().plusHours(24));
        idempotencyStore.store(idempotencyEntry);

        // Publie l'event "payment.initiated" sur payment-events
        PaymentEvent event = new PaymentEvent();
        event.setEventType("payment.initiated");
        event.setPaymentId(saved.getId());
        event.setPaymentReference(saved.getPaymentReference());
        event.setSenderWalletId(saved.getSenderWalletId());
        event.setReceiverWalletId(saved.getReceiverWalletId());
        event.setAmount(saved.getAmount());
        event.setFeeAmount(saved.getFeeAmount());
        event.setCurrency(saved.getCurrency());
        event.setStatus(saved.getStatus());
        event.setType(saved.getType());
        event.setIdempotencyKey(saved.getIdempotencyKey());
        event.setOccurredAt(OffsetDateTime.now());
        eventPublisher.publish(event);

        // Démarre le Saga par le fraud check (étape 0)
        sagaOrchestrator.startFraudCheck(saved);

        return saved;
    }

    private String generateReference() {
        String datePart = java.time.LocalDate.now().toString().replace("-", "");
        String randPart = String.valueOf(System.currentTimeMillis()).substring(5);
        return "PAY-" + datePart + "-" + randPart;
    }
}
