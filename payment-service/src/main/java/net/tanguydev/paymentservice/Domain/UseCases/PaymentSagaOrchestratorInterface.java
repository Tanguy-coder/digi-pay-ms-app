package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;

import java.util.UUID;

public interface PaymentSagaOrchestratorInterface {

    void startSaga(DomainPayment payment);

    void onDebitSuccess(UUID paymentId);

    void onDebitFailure(UUID paymentId, String reason);

    void onCreditSuccess(UUID paymentId);

    void onCreditFailure(UUID paymentId, String reason);

    void onCompensationCompleted(UUID paymentId);

    void onCompensationFailed(UUID paymentId, String reason);
}
