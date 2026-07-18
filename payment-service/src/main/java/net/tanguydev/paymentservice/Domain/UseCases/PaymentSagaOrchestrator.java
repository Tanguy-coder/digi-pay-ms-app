package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Commands.WalletCommand;
import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Entities.DomainSagaStep;
import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Enums.SagaStepName;
import net.tanguydev.paymentservice.Domain.Enums.SagaStepStatus;
import net.tanguydev.paymentservice.Domain.Events.PaymentEvent;
import net.tanguydev.paymentservice.Domain.Gateways.SagaStepRepositoryInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentEventPublisherInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentServiceInterface;
import net.tanguydev.paymentservice.Domain.Ports.WalletCommandPublisherInterface;
import net.tanguydev.paymentservice.Domain.Validations.Exception.PaymentNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentSagaOrchestrator implements PaymentSagaOrchestratorInterface {

    private final PaymentServiceInterface paymentService;
    private final SagaStepRepositoryInterface sagaStepRepository;
    private final WalletCommandPublisherInterface commandPublisher;
    private final PaymentEventPublisherInterface eventPublisher;

    public PaymentSagaOrchestrator(PaymentServiceInterface paymentService,
                                   SagaStepRepositoryInterface sagaStepRepository,
                                   WalletCommandPublisherInterface commandPublisher,
                                   PaymentEventPublisherInterface eventPublisher) {
        this.paymentService = paymentService;
        this.sagaStepRepository = sagaStepRepository;
        this.commandPublisher = commandPublisher;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Étape 0 : crée l'étape FRAUD_CHECK et publie payment.initiated pour le fraud-service.
     * Appelé juste après la création du paiement dans InitiatePaymentUseCase.
     */
    @Override
    public void startFraudCheck(DomainPayment payment) {
        sagaStepRepository.save(buildStep(payment.getId(), SagaStepName.FRAUD_CHECK, 0, null));

        DomainPayment updated = loadPayment(payment.getId());
        updated.setStatus(PaymentStatus.FRAUD_CHECK);
        paymentService.save(updated);

        markStepStarted(payment.getId(), SagaStepName.FRAUD_CHECK);
    }

    /**
     * Le fraud-service a retourné CLEARED → on démarre le Saga normal.
     */
    @Override
    public void onFraudCleared(UUID paymentId) {
        markStepCompleted(paymentId, SagaStepName.FRAUD_CHECK);
        DomainPayment payment = loadPayment(paymentId);
        startSaga(payment);
    }

    /**
     * Le fraud-service a retourné BLOCKED → paiement FAILED immédiatement.
     */
    @Override
    public void onFraudBlocked(UUID paymentId, String reason) {
        markStepFailed(paymentId, SagaStepName.FRAUD_CHECK, reason);

        DomainPayment payment = loadPayment(paymentId);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("FRAUD_BLOCKED: " + reason);
        payment.setCompletedAt(OffsetDateTime.now());
        paymentService.save(payment);

        publishEvent("payment.failed", payment);
    }

    /**
     * Démarre le Saga : crée les 4 étapes en base puis lance le débit du wallet emetteur.
     */
    @Override
    public void startSaga(DomainPayment payment) {
        sagaStepRepository.save(buildStep(payment.getId(), SagaStepName.DEBIT_SENDER, 1, "saga.compensate_debit"));
        sagaStepRepository.save(buildStep(payment.getId(), SagaStepName.CREDIT_RECEIVER, 2, null));
        sagaStepRepository.save(buildStep(payment.getId(), SagaStepName.NOTIFY, 3, null));
        sagaStepRepository.save(buildStep(payment.getId(), SagaStepName.SETTLE, 4, null));

        // Lance l'étape 1 : envoie la commande DEBIT au wallet-service via Kafka
        WalletCommand command = new WalletCommand();
        command.setCommandType("DEBIT");
        command.setPaymentId(payment.getId());
        command.setWalletId(payment.getSenderWalletId());
        command.setAmount(payment.getAmount());
        command.setCurrency(payment.getCurrency());
        commandPublisher.publish(command);

        markStepStarted(payment.getId(), SagaStepName.DEBIT_SENDER);
    }

    /**
     * Le wallet-service a confirmé le débit.
     * → On passe le paiement en PROCESSING et on lance le crédit du destinataire.
     */
    @Override
    public void onDebitSuccess(UUID paymentId) {
        markStepCompleted(paymentId, SagaStepName.DEBIT_SENDER);

        DomainPayment payment = loadPayment(paymentId);
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentService.save(payment);

        // Lance l'étape 2 : envoie la commande CREDIT au wallet-service
        WalletCommand command = new WalletCommand();
        command.setCommandType("CREDIT");
        command.setPaymentId(paymentId);
        command.setWalletId(payment.getReceiverWalletId());
        command.setAmount(payment.getNetAmount());
        command.setCurrency(payment.getCurrency());
        commandPublisher.publish(command);

        markStepStarted(paymentId, SagaStepName.CREDIT_RECEIVER);
    }

    /**
     * Le wallet-service a échoué à débiter (solde insuffisant, wallet inactif…).
     * → Rien à annuler, le débit n'a pas eu lieu. On marque FAILED directement.
     */
    @Override
    public void onDebitFailure(UUID paymentId, String reason) {
        markStepFailed(paymentId, SagaStepName.DEBIT_SENDER, reason);

        DomainPayment payment = loadPayment(paymentId);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment.setCompletedAt(OffsetDateTime.now());
        paymentService.save(payment);

        publishEvent("payment.failed", payment);
    }

    /**
     * Le wallet-service a confirmé le crédit.
     * → On complète les étapes NOTIFY et SETTLE automatiquement,
     *   et on marque le paiement COMPLETED.
     */
    @Override
    public void onCreditSuccess(UUID paymentId) {
        markStepCompleted(paymentId, SagaStepName.CREDIT_RECEIVER);

        // NOTIFY et SETTLE sont auto-complétés (pas de service externe pour l'instant)
        markStepCompleted(paymentId, SagaStepName.NOTIFY);
        markStepCompleted(paymentId, SagaStepName.SETTLE);

        DomainPayment payment = loadPayment(paymentId);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(OffsetDateTime.now());
        paymentService.save(payment);

        publishEvent("payment.completed", payment);
    }

    /**
     * Le wallet-service a échoué à créditer le destinataire.
     * → COMPENSATION : on re-crédite l'emetteur pour annuler le débit de l'étape 1.
     */
    @Override
    public void onCreditFailure(UUID paymentId, String reason) {
        markStepFailed(paymentId, SagaStepName.CREDIT_RECEIVER, reason);

        DomainPayment payment = loadPayment(paymentId);

        // Envoie la commande de compensation : re-créditer le wallet emetteur
        WalletCommand compensation = new WalletCommand();
        compensation.setCommandType("COMPENSATE_DEBIT");
        compensation.setPaymentId(paymentId);
        compensation.setWalletId(payment.getSenderWalletId());
        compensation.setAmount(payment.getAmount());
        compensation.setCurrency(payment.getCurrency());
        commandPublisher.publish(compensation);
    }

    /**
     * La compensation a réussi : l'emetteur a été re-crédité.
     * → Paiement marqué REVERSED.
     */
    @Override
    public void onCompensationCompleted(UUID paymentId) {
        markStepCompleted(paymentId, SagaStepName.DEBIT_SENDER);

        DomainPayment payment = loadPayment(paymentId);
        payment.setStatus(PaymentStatus.REVERSED);
        payment.setCompletedAt(OffsetDateTime.now());
        paymentService.save(payment);

        publishEvent("payment.reversed", payment);
    }

    /**
     * La compensation elle-même a échoué — cas critique, nécessite intervention manuelle.
     * On marque FAILED avec un message d'erreur explicite.
     */
    @Override
    public void onCompensationFailed(UUID paymentId, String reason) {
        DomainPayment payment = loadPayment(paymentId);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("COMPENSATION_FAILED: " + reason);
        payment.setCompletedAt(OffsetDateTime.now());
        paymentService.save(payment);

        publishEvent("payment.compensation_failed", payment);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private DomainPayment loadPayment(UUID paymentId) {
        return paymentService.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private DomainSagaStep buildStep(UUID paymentId, SagaStepName name, int order, String compensationEvent) {
        DomainSagaStep step = new DomainSagaStep();
        step.setPaymentId(paymentId);
        step.setStepName(name);
        step.setStepStatus(SagaStepStatus.PENDING);
        step.setStepOrder(order);
        step.setCompensationEvent(compensationEvent);
        step.setRetryCount(0);
        step.setMaxRetries(3);
        return step;
    }

    private void markStepStarted(UUID paymentId, SagaStepName name) {
        findStep(paymentId, name).ifPresent(step -> {
            step.setStepStatus(SagaStepStatus.STARTED);
            step.setStartedAt(OffsetDateTime.now());
            sagaStepRepository.save(step);
        });
    }

    private void markStepCompleted(UUID paymentId, SagaStepName name) {
        findStep(paymentId, name).ifPresent(step -> {
            step.setStepStatus(SagaStepStatus.COMPLETED);
            step.setCompletedAt(OffsetDateTime.now());
            sagaStepRepository.save(step);
        });
    }

    private void markStepFailed(UUID paymentId, SagaStepName name, String reason) {
        findStep(paymentId, name).ifPresent(step -> {
            step.setStepStatus(SagaStepStatus.FAILED);
            step.setError(reason);
            step.setCompletedAt(OffsetDateTime.now());
            sagaStepRepository.save(step);
        });
    }

    private java.util.Optional<DomainSagaStep> findStep(UUID paymentId, SagaStepName name) {
        return sagaStepRepository.findByPaymentId(paymentId).stream()
                .filter(s -> s.getStepName() == name)
                .findFirst();
    }

    private void publishEvent(String eventType, DomainPayment payment) {
        PaymentEvent event = new PaymentEvent();
        event.setEventType(eventType);
        event.setPaymentId(payment.getId());
        event.setPaymentReference(payment.getPaymentReference());
        event.setSenderWalletId(payment.getSenderWalletId());
        event.setReceiverWalletId(payment.getReceiverWalletId());
        event.setAmount(payment.getAmount());
        event.setFeeAmount(payment.getFeeAmount());
        event.setCurrency(payment.getCurrency());
        event.setStatus(payment.getStatus());
        event.setType(payment.getType());
        event.setIdempotencyKey(payment.getIdempotencyKey());
        event.setOccurredAt(OffsetDateTime.now());
        eventPublisher.publish(event);
    }
}
