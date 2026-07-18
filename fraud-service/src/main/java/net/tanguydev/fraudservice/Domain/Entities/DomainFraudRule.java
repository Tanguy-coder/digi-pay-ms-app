package net.tanguydev.fraudservice.Domain.Entities;

import net.tanguydev.fraudservice.Domain.Enums.RuleAction;
import net.tanguydev.fraudservice.Domain.Enums.RulePriority;
import net.tanguydev.fraudservice.Domain.Enums.RuleType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DomainFraudRule {

    private UUID id;
    private String ruleCode;
    private String description;
    private RuleType ruleType;
    private BigDecimal thresholdValue;
    private BigDecimal scoreWeight;
    private RuleAction action;
    private RulePriority priority;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public DomainFraudRule() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RuleType getRuleType() { return ruleType; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }

    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }

    public BigDecimal getScoreWeight() { return scoreWeight; }
    public void setScoreWeight(BigDecimal scoreWeight) { this.scoreWeight = scoreWeight; }

    public RuleAction getAction() { return action; }
    public void setAction(RuleAction action) { this.action = action; }

    public RulePriority getPriority() { return priority; }
    public void setPriority(RulePriority priority) { this.priority = priority; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
