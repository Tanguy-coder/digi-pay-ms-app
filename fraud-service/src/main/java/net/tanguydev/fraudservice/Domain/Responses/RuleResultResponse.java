package net.tanguydev.fraudservice.Domain.Responses;

import java.math.BigDecimal;

public class RuleResultResponse {

    private String ruleCode;
    private BigDecimal scoreContribution;
    private String reason;

    public RuleResultResponse() {}

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public BigDecimal getScoreContribution() { return scoreContribution; }
    public void setScoreContribution(BigDecimal scoreContribution) { this.scoreContribution = scoreContribution; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
