package net.tanguydev.fraudservice.Domain.Services;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudRule;
import net.tanguydev.fraudservice.Domain.Enums.FraudVerdict;
import net.tanguydev.fraudservice.Domain.Enums.RuleAction;
import net.tanguydev.fraudservice.Domain.Enums.RuleType;
import net.tanguydev.fraudservice.Domain.Services.FraudRulesEngine.EngineResult;
import net.tanguydev.fraudservice.Domain.Services.FraudRulesEngine.EvaluationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FraudRulesEngineTest {

    private FraudRulesEngine engine;

    @BeforeEach
    void setUp() {
        engine = new FraudRulesEngine();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private DomainFraudRule rule(String code, RuleType type, BigDecimal threshold,
                                  BigDecimal weight, RuleAction action) {
        DomainFraudRule r = new DomainFraudRule();
        r.setRuleCode(code);
        r.setRuleType(type);
        r.setThresholdValue(threshold);
        r.setScoreWeight(weight);
        r.setAction(action);
        r.setActive(true);
        return r;
    }

    private EvaluationContext ctx(BigDecimal amount, String country,
                                   Boolean isNewDevice, Integer hour,
                                   int vel1min, int vel1h) {
        return new EvaluationContext(amount, "EUR", country, "fp-test",
                isNewDevice, hour, vel1min, vel1h);
    }

    // ── verdict thresholds ─────────────────────────────────────────────────────

    @Test
    void noRulesTriggered_returnsCleared() {
        DomainFraudRule amountRule = rule("HIGH_AMOUNT", RuleType.AMOUNT,
                new BigDecimal("10000"), new BigDecimal("85"), RuleAction.BLOCK);

        EvaluationContext ctx = ctx(new BigDecimal("100"), "FR", false, 10, 0, 0);
        EngineResult result = engine.evaluate(List.of(amountRule), ctx);

        assertThat(result.verdict()).isEqualTo(FraudVerdict.CLEARED);
        assertThat(result.riskScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.rulesTriggered()).isEmpty();
    }

    @Test
    void scoreBelow31_returnsCleared() {
        DomainFraudRule rule = rule("SUSPICIOUS_HOUR", RuleType.BEHAVIORAL,
                BigDecimal.ZERO, new BigDecimal("15"), RuleAction.FLAG);

        EvaluationContext ctx = ctx(new BigDecimal("50"), "FR", false, 2, 0, 0);
        EngineResult result = engine.evaluate(List.of(rule), ctx);

        assertThat(result.verdict()).isEqualTo(FraudVerdict.CLEARED);
        assertThat(result.riskScore()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void scoreBetween31And60_returnsReview() {
        DomainFraudRule rule = rule("VELOCITY_1MIN", RuleType.VELOCITY,
                new BigDecimal("3"), new BigDecimal("40"), RuleAction.REVIEW);

        EvaluationContext ctx = ctx(new BigDecimal("100"), "FR", false, 10, 5, 0);
        EngineResult result = engine.evaluate(List.of(rule), ctx);

        assertThat(result.verdict()).isEqualTo(FraudVerdict.REVIEW);
        assertThat(result.riskScore()).isEqualByComparingTo(new BigDecimal("40.00"));
    }

    @Test
    void scoreBetween61And80_returnsFlagged() {
        DomainFraudRule r1 = rule("VELOCITY_1MIN", RuleType.VELOCITY,
                new BigDecimal("3"), new BigDecimal("40"), RuleAction.REVIEW);
        DomainFraudRule r2 = rule("VELOCITY_1H", RuleType.VELOCITY,
                new BigDecimal("10"), new BigDecimal("25"), RuleAction.FLAG);

        EvaluationContext ctx = ctx(new BigDecimal("100"), "FR", false, 10, 5, 12);
        EngineResult result = engine.evaluate(List.of(r1, r2), ctx);

        assertThat(result.verdict()).isEqualTo(FraudVerdict.FLAGGED);
        assertThat(result.riskScore()).isEqualByComparingTo(new BigDecimal("65.00"));
    }

    @Test
    void scoreAbove80_returnsBlocked() {
        DomainFraudRule r1 = rule("VELOCITY_1MIN", RuleType.VELOCITY,
                new BigDecimal("3"), new BigDecimal("40"), RuleAction.REVIEW);
        DomainFraudRule r2 = rule("HIGH_AMOUNT", RuleType.AMOUNT,
                new BigDecimal("10000"), new BigDecimal("85"), RuleAction.FLAG);

        EvaluationContext ctx = ctx(new BigDecimal("15000"), "FR", false, 10, 5, 0);
        EngineResult result = engine.evaluate(List.of(r1, r2), ctx);

        assertThat(result.verdict()).isEqualTo(FraudVerdict.BLOCKED);
    }

    // ── hard block ─────────────────────────────────────────────────────────────

    @Test
    void ruleWithActionBlock_forcesBlockedRegardlessOfScore() {
        DomainFraudRule rule = rule("RISKY_COUNTRY_KP", RuleType.GEOLOCATION,
                BigDecimal.ZERO, new BigDecimal("20"), RuleAction.BLOCK);

        EvaluationContext ctx = ctx(new BigDecimal("10"), "KP", false, 10, 0, 0);
        EngineResult result = engine.evaluate(List.of(rule), ctx);

        assertThat(result.verdict()).isEqualTo(FraudVerdict.BLOCKED);
    }

    // ── score cap ──────────────────────────────────────────────────────────────

    @Test
    void totalScoreCannotExceed100() {
        DomainFraudRule r1 = rule("HIGH_AMOUNT", RuleType.AMOUNT,
                new BigDecimal("10000"), new BigDecimal("85"), RuleAction.FLAG);
        DomainFraudRule r2 = rule("VELOCITY_1MIN", RuleType.VELOCITY,
                new BigDecimal("3"), new BigDecimal("40"), RuleAction.REVIEW);

        EvaluationContext ctx = ctx(new BigDecimal("15000"), "FR", false, 10, 5, 0);
        EngineResult result = engine.evaluate(List.of(r1, r2), ctx);

        assertThat(result.riskScore()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    // ── rule types ─────────────────────────────────────────────────────────────

    @Test
    void amountRule_notTriggered_whenAmountBelowThreshold() {
        DomainFraudRule rule = rule("HIGH_AMOUNT", RuleType.AMOUNT,
                new BigDecimal("10000"), new BigDecimal("85"), RuleAction.BLOCK);

        EvaluationContext ctx = ctx(new BigDecimal("500"), "FR", false, 10, 0, 0);
        EngineResult result = engine.evaluate(List.of(rule), ctx);

        assertThat(result.rulesTriggered()).isEmpty();
    }

    @Test
    void velocityRule_triggeredWhenVelocityReachesThreshold() {
        DomainFraudRule rule = rule("VELOCITY_1MIN", RuleType.VELOCITY,
                new BigDecimal("3"), new BigDecimal("40"), RuleAction.REVIEW);

        EvaluationContext ctx = ctx(new BigDecimal("100"), "FR", false, 10, 3, 0);
        EngineResult result = engine.evaluate(List.of(rule), ctx);

        assertThat(result.rulesTriggered()).hasSize(1);
        assertThat(result.rulesTriggered().get(0).getRuleCode()).isEqualTo("VELOCITY_1MIN");
    }

    @Test
    void geolocationRule_notTriggered_whenCountryDoesNotMatch() {
        DomainFraudRule rule = rule("RISKY_COUNTRY_KP", RuleType.GEOLOCATION,
                BigDecimal.ZERO, new BigDecimal("90"), RuleAction.BLOCK);

        EvaluationContext ctx = ctx(new BigDecimal("100"), "FR", false, 10, 0, 0);
        EngineResult result = engine.evaluate(List.of(rule), ctx);

        assertThat(result.rulesTriggered()).isEmpty();
    }

    @Test
    void deviceRule_triggered_whenNewDevice() {
        DomainFraudRule rule = rule("NEW_DEVICE", RuleType.DEVICE,
                BigDecimal.ZERO, new BigDecimal("20"), RuleAction.CHALLENGE_OTP);

        EvaluationContext ctx = ctx(new BigDecimal("100"), "FR", true, 10, 0, 0);
        EngineResult result = engine.evaluate(List.of(rule), ctx);

        assertThat(result.rulesTriggered()).hasSize(1);
    }

    @Test
    void behavioralRule_triggered_atSuspiciousHour() {
        DomainFraudRule rule = rule("SUSPICIOUS_HOUR", RuleType.BEHAVIORAL,
                BigDecimal.ZERO, new BigDecimal("15"), RuleAction.FLAG);

        EvaluationContext ctx = ctx(new BigDecimal("100"), "FR", false, 3, 0, 0);
        EngineResult result = engine.evaluate(List.of(rule), ctx);

        assertThat(result.rulesTriggered()).hasSize(1);
    }

    @Test
    void behavioralRule_notTriggered_atNormalHour() {
        DomainFraudRule rule = rule("SUSPICIOUS_HOUR", RuleType.BEHAVIORAL,
                BigDecimal.ZERO, new BigDecimal("15"), RuleAction.FLAG);

        EvaluationContext ctx = ctx(new BigDecimal("100"), "FR", false, 14, 0, 0);
        EngineResult result = engine.evaluate(List.of(rule), ctx);

        assertThat(result.rulesTriggered()).isEmpty();
    }
}
