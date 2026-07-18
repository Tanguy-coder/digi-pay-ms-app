package net.tanguydev.fraudservice.Domain.Services;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis.RuleEvaluationResult;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudRule;
import net.tanguydev.fraudservice.Domain.Enums.FraudVerdict;
import net.tanguydev.fraudservice.Domain.Enums.RuleAction;
import net.tanguydev.fraudservice.Domain.Enums.RuleType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Évalue toutes les règles actives contre le contexte d'un paiement
 * et calcule le risk_score + verdict final.
 *
 * Règle de verdict :
 *   0–30   → CLEARED
 *   31–60  → REVIEW
 *   61–80  → FLAGGED
 *   81–100 → BLOCKED
 *
 * Une règle avec action=BLOCK déclenche BLOCKED immédiatement,
 * peu importe le score cumulé.
 */
public class FraudRulesEngine {

    public EngineResult evaluate(List<DomainFraudRule> activeRules, EvaluationContext ctx) {
        List<RuleEvaluationResult> triggered = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        boolean hardBlock = false;

        for (DomainFraudRule rule : activeRules) {
            EvalResult result = evaluateRule(rule, ctx);
            if (result.triggered) {
                triggered.add(new RuleEvaluationResult(rule.getRuleCode(), rule.getScoreWeight(), result.reason));
                totalScore = totalScore.add(rule.getScoreWeight());
                if (rule.getAction() == RuleAction.BLOCK) {
                    hardBlock = true;
                }
            }
        }

        BigDecimal finalScore = totalScore.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
        FraudVerdict verdict = hardBlock ? FraudVerdict.BLOCKED : computeVerdict(finalScore);

        return new EngineResult(finalScore, verdict, triggered);
    }

    private EvalResult evaluateRule(DomainFraudRule rule, EvaluationContext ctx) {
        return switch (rule.getRuleType()) {
            case AMOUNT      -> evaluateAmount(rule, ctx);
            case VELOCITY    -> evaluateVelocity(rule, ctx);
            case GEOLOCATION -> evaluateGeolocation(rule, ctx);
            case DEVICE      -> evaluateDevice(rule, ctx);
            case BEHAVIORAL  -> evaluateBehavioral(rule, ctx);
        };
    }

    private EvalResult evaluateAmount(DomainFraudRule rule, EvaluationContext ctx) {
        if (ctx.amount().compareTo(rule.getThresholdValue()) > 0) {
            return EvalResult.triggered("Amount " + ctx.amount() + " exceeds threshold " + rule.getThresholdValue());
        }
        return EvalResult.notTriggered();
    }

    private EvalResult evaluateVelocity(DomainFraudRule rule, EvaluationContext ctx) {
        // VELOCITY_1MIN : threshold = max tx/min, VELOCITY_1H : threshold = max tx/h
        // On distingue par ruleCode convention
        int count = rule.getRuleCode().contains("1MIN") ? ctx.velocity1min() : ctx.velocity1h();
        if (count >= rule.getThresholdValue().intValue()) {
            return EvalResult.triggered("Velocity " + count + " tx >= threshold " + rule.getThresholdValue().intValue());
        }
        return EvalResult.notTriggered();
    }

    private EvalResult evaluateGeolocation(DomainFraudRule rule, EvaluationContext ctx) {
        // threshold_value non utilisée ici — la liste des pays risqués est dans ruleCode
        // Convention : ruleCode = "RISKY_COUNTRY_XX" où XX = code pays
        if (ctx.countryCode() != null && rule.getRuleCode().endsWith(ctx.countryCode())) {
            return EvalResult.triggered("Transaction from risky country: " + ctx.countryCode());
        }
        return EvalResult.notTriggered();
    }

    private EvalResult evaluateDevice(DomainFraudRule rule, EvaluationContext ctx) {
        if (Boolean.TRUE.equals(ctx.isNewDevice())) {
            return EvalResult.triggered("Payment from unknown device");
        }
        return EvalResult.notTriggered();
    }

    private EvalResult evaluateBehavioral(DomainFraudRule rule, EvaluationContext ctx) {
        // Heure suspecte : transaction entre 0h et 5h UTC
        if (ctx.transactionHour() != null && ctx.transactionHour() >= 0 && ctx.transactionHour() <= 5) {
            return EvalResult.triggered("Suspicious transaction hour: " + ctx.transactionHour() + "h UTC");
        }
        return EvalResult.notTriggered();
    }

    private FraudVerdict computeVerdict(BigDecimal score) {
        int s = score.intValue();
        if (s <= 30) return FraudVerdict.CLEARED;
        if (s <= 60) return FraudVerdict.REVIEW;
        if (s <= 80) return FraudVerdict.FLAGGED;
        return FraudVerdict.BLOCKED;
    }

    // ── value objects ──────────────────────────────────────────────────────────

    public record EvaluationContext(
            BigDecimal amount,
            String currency,
            String countryCode,
            String deviceFingerprint,
            Boolean isNewDevice,
            Integer transactionHour,
            int velocity1min,
            int velocity1h
    ) {}

    public record EngineResult(
            BigDecimal riskScore,
            FraudVerdict verdict,
            List<RuleEvaluationResult> rulesTriggered
    ) {}

    private record EvalResult(boolean triggered, String reason) {
        static EvalResult triggered(String reason) { return new EvalResult(true, reason); }
        static EvalResult notTriggered() { return new EvalResult(false, null); }
    }
}
