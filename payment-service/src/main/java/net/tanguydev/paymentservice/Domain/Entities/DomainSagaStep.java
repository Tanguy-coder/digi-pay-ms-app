package net.tanguydev.paymentservice.Domain.Entities;

import net.tanguydev.paymentservice.Domain.Enums.SagaStepName;
import net.tanguydev.paymentservice.Domain.Enums.SagaStepStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public class DomainSagaStep {

    private UUID id;
    private UUID paymentId;
    private SagaStepName stepName;
    private SagaStepStatus stepStatus;
    private int stepOrder;
    private String compensationEvent;
    private int retryCount;
    private int maxRetries;
    private String error;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;

    public DomainSagaStep() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public SagaStepName getStepName() { return stepName; }
    public void setStepName(SagaStepName stepName) { this.stepName = stepName; }

    public SagaStepStatus getStepStatus() { return stepStatus; }
    public void setStepStatus(SagaStepStatus stepStatus) { this.stepStatus = stepStatus; }

    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }

    public String getCompensationEvent() { return compensationEvent; }
    public void setCompensationEvent(String compensationEvent) { this.compensationEvent = compensationEvent; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
