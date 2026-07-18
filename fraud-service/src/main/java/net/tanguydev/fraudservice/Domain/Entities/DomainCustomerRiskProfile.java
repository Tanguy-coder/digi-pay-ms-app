package net.tanguydev.fraudservice.Domain.Entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DomainCustomerRiskProfile {

    private UUID customerId;
    private BigDecimal lifetimeRiskScore;
    private Integer totalAlerts;
    private Integer falsePositives;
    private boolean blacklisted;
    private BigDecimal avgTransactionAmount;
    private OffsetDateTime lastAnalysisAt;
    private OffsetDateTime updatedAt;

    public DomainCustomerRiskProfile() {}

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public BigDecimal getLifetimeRiskScore() { return lifetimeRiskScore; }
    public void setLifetimeRiskScore(BigDecimal lifetimeRiskScore) { this.lifetimeRiskScore = lifetimeRiskScore; }

    public Integer getTotalAlerts() { return totalAlerts; }
    public void setTotalAlerts(Integer totalAlerts) { this.totalAlerts = totalAlerts; }

    public Integer getFalsePositives() { return falsePositives; }
    public void setFalsePositives(Integer falsePositives) { this.falsePositives = falsePositives; }

    public boolean isBlacklisted() { return blacklisted; }
    public void setBlacklisted(boolean blacklisted) { this.blacklisted = blacklisted; }

    public BigDecimal getAvgTransactionAmount() { return avgTransactionAmount; }
    public void setAvgTransactionAmount(BigDecimal avgTransactionAmount) { this.avgTransactionAmount = avgTransactionAmount; }

    public OffsetDateTime getLastAnalysisAt() { return lastAnalysisAt; }
    public void setLastAnalysisAt(OffsetDateTime lastAnalysisAt) { this.lastAnalysisAt = lastAnalysisAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
