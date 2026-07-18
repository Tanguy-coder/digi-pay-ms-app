package net.tanguydev.fraudservice.Domain.Responses;

import net.tanguydev.fraudservice.Domain.Enums.FraudVerdict;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class FraudAnalysisResponse {

    private UUID id;
    private UUID paymentId;
    private UUID customerId;
    private BigDecimal riskScore;
    private FraudVerdict verdict;
    private Integer analysisDurationMs;
    private List<RuleResultResponse> rulesTriggered;
    private String countryCode;
    private Integer velocity1min;
    private Integer velocity1h;
    private OffsetDateTime createdAt;

    public FraudAnalysisResponse() {}

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

    public List<RuleResultResponse> getRulesTriggered() { return rulesTriggered; }
    public void setRulesTriggered(List<RuleResultResponse> rulesTriggered) { this.rulesTriggered = rulesTriggered; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public Integer getVelocity1min() { return velocity1min; }
    public void setVelocity1min(Integer velocity1min) { this.velocity1min = velocity1min; }

    public Integer getVelocity1h() { return velocity1h; }
    public void setVelocity1h(Integer velocity1h) { this.velocity1h = velocity1h; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
