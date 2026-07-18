package net.tanguydev.fraudservice.Domain.Entities;

import net.tanguydev.fraudservice.Domain.Enums.FraudVerdict;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class DomainFraudAnalysis {

    private UUID id;
    private UUID paymentId;
    private UUID customerId;
    private BigDecimal riskScore;
    private FraudVerdict verdict;
    private Integer analysisDurationMs;
    private List<RuleEvaluationResult> rulesTriggered;
    private String ipAddress;
    private String countryCode;
    private String deviceFingerprint;
    private Boolean isNewDevice;
    private Integer transactionHour;
    private Integer velocity1min;
    private Integer velocity1h;
    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime createdAt;

    public DomainFraudAnalysis() {}

    public static class RuleEvaluationResult {
        private String ruleCode;
        private BigDecimal scoreContribution;
        private String reason;

        public RuleEvaluationResult(String ruleCode, BigDecimal scoreContribution, String reason) {
            this.ruleCode = ruleCode;
            this.scoreContribution = scoreContribution;
            this.reason = reason;
        }

        public String getRuleCode() { return ruleCode; }
        public BigDecimal getScoreContribution() { return scoreContribution; }
        public String getReason() { return reason; }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

    public FraudVerdict getVerdict() { return verdict; }
    public void setVerdict(FraudVerdict verdict) { this.verdict = verdict; }

    public Integer getAnalysisDurationMs() { return analysisDurationMs; }
    public void setAnalysisDurationMs(Integer analysisDurationMs) { this.analysisDurationMs = analysisDurationMs; }

    public List<RuleEvaluationResult> getRulesTriggered() { return rulesTriggered; }
    public void setRulesTriggered(List<RuleEvaluationResult> rulesTriggered) { this.rulesTriggered = rulesTriggered; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }

    public Boolean getIsNewDevice() { return isNewDevice; }
    public void setIsNewDevice(Boolean isNewDevice) { this.isNewDevice = isNewDevice; }

    public Integer getTransactionHour() { return transactionHour; }
    public void setTransactionHour(Integer transactionHour) { this.transactionHour = transactionHour; }

    public Integer getVelocity1min() { return velocity1min; }
    public void setVelocity1min(Integer velocity1min) { this.velocity1min = velocity1min; }

    public Integer getVelocity1h() { return velocity1h; }
    public void setVelocity1h(Integer velocity1h) { this.velocity1h = velocity1h; }

    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }

    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
