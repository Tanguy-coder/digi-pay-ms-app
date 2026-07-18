package net.tanguydev.fraudservice.Domain.Entities;

import net.tanguydev.fraudservice.Domain.Enums.AlertStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DomainFraudAlert {

    private UUID id;
    private UUID fraudAnalysisId;
    private UUID fraudRuleId;
    private AlertStatus alertStatus;
    private BigDecimal scoreAtTrigger;
    private String resolutionNote;
    private OffsetDateTime triggeredAt;
    private OffsetDateTime resolvedAt;

    public DomainFraudAlert() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getFraudAnalysisId() { return fraudAnalysisId; }
    public void setFraudAnalysisId(UUID fraudAnalysisId) { this.fraudAnalysisId = fraudAnalysisId; }

    public UUID getFraudRuleId() { return fraudRuleId; }
    public void setFraudRuleId(UUID fraudRuleId) { this.fraudRuleId = fraudRuleId; }

    public AlertStatus getAlertStatus() { return alertStatus; }
    public void setAlertStatus(AlertStatus alertStatus) { this.alertStatus = alertStatus; }

    public BigDecimal getScoreAtTrigger() { return scoreAtTrigger; }
    public void setScoreAtTrigger(BigDecimal scoreAtTrigger) { this.scoreAtTrigger = scoreAtTrigger; }

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }

    public OffsetDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(OffsetDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
