package net.tanguydev.fraudservice.Domain.UseCases;

import net.tanguydev.fraudservice.Domain.Entities.DomainCustomerRiskProfile;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAlert;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudRule;
import net.tanguydev.fraudservice.Domain.Enums.AlertStatus;
import net.tanguydev.fraudservice.Domain.Enums.FraudVerdict;
import net.tanguydev.fraudservice.Domain.Enums.RuleAction;
import net.tanguydev.fraudservice.Domain.Ports.CustomerRiskProfileRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudAlertRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudAnalysisRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudEventPublisherInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudRuleRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.VelocityCounterInterface;
import net.tanguydev.fraudservice.Domain.Services.FraudRulesEngine;
import net.tanguydev.fraudservice.Domain.Services.FraudRulesEngine.EngineResult;
import net.tanguydev.fraudservice.Domain.Services.FraudRulesEngine.EvaluationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public class AnalyzePaymentUseCase implements AnalyzePaymentUseCaseInterface {

    private final FraudRuleRepositoryInterface ruleRepository;
    private final FraudAnalysisRepositoryInterface analysisRepository;
    private final FraudAlertRepositoryInterface alertRepository;
    private final CustomerRiskProfileRepositoryInterface riskProfileRepository;
    private final FraudEventPublisherInterface eventPublisher;
    private final VelocityCounterInterface velocityCounter;
    private final FraudRulesEngine rulesEngine;

    public AnalyzePaymentUseCase(FraudRuleRepositoryInterface ruleRepository,
                                  FraudAnalysisRepositoryInterface analysisRepository,
                                  FraudAlertRepositoryInterface alertRepository,
                                  CustomerRiskProfileRepositoryInterface riskProfileRepository,
                                  FraudEventPublisherInterface eventPublisher,
                                  VelocityCounterInterface velocityCounter,
                                  FraudRulesEngine rulesEngine) {
        this.ruleRepository = ruleRepository;
        this.analysisRepository = analysisRepository;
        this.alertRepository = alertRepository;
        this.riskProfileRepository = riskProfileRepository;
        this.eventPublisher = eventPublisher;
        this.velocityCounter = velocityCounter;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public DomainFraudAnalysis execute(AnalyzePaymentCommand command) {
        long startMs = System.currentTimeMillis();

        List<DomainFraudRule> activeRules = ruleRepository.findAllActive();

        int velocity1min = velocityCounter.countLastMinute(command.getSenderWalletId());
        int velocity1h   = velocityCounter.countLastHour(command.getSenderWalletId());
        int txHour       = OffsetDateTime.now(ZoneOffset.UTC).getHour();

        Optional<DomainCustomerRiskProfile> existingProfile =
                riskProfileRepository.findByCustomerId(command.getCustomerId());
        Boolean isNewDevice = isNewDevice(command.getDeviceFingerprint(), existingProfile);

        EvaluationContext ctx = new EvaluationContext(
                command.getAmount(),
                command.getCurrency(),
                command.getCountryCode(),
                command.getDeviceFingerprint(),
                isNewDevice,
                txHour,
                velocity1min,
                velocity1h
        );

        EngineResult engineResult = rulesEngine.evaluate(activeRules, ctx);

        int durationMs = (int) (System.currentTimeMillis() - startMs);

        DomainFraudAnalysis analysis = buildAnalysis(command, ctx, engineResult, durationMs);
        DomainFraudAnalysis saved = analysisRepository.save(analysis);

        createAlertsForTriggeredRules(saved, activeRules, engineResult);
        updateRiskProfile(saved, existingProfile);
        eventPublisher.publish(saved);

        return saved;
    }

    private DomainFraudAnalysis buildAnalysis(AnalyzePaymentCommand command,
                                               EvaluationContext ctx,
                                               EngineResult result,
                                               int durationMs) {
        DomainFraudAnalysis analysis = new DomainFraudAnalysis();
        analysis.setPaymentId(command.getPaymentId());
        analysis.setCustomerId(command.getCustomerId());
        analysis.setRiskScore(result.riskScore());
        analysis.setVerdict(result.verdict());
        analysis.setAnalysisDurationMs(durationMs);
        analysis.setRulesTriggered(result.rulesTriggered());
        analysis.setIpAddress(command.getIpAddress());
        analysis.setCountryCode(command.getCountryCode());
        analysis.setDeviceFingerprint(command.getDeviceFingerprint());
        analysis.setIsNewDevice(ctx.isNewDevice());
        analysis.setTransactionHour(ctx.transactionHour());
        analysis.setVelocity1min(ctx.velocity1min());
        analysis.setVelocity1h(ctx.velocity1h());
        analysis.setCreatedAt(OffsetDateTime.now());
        return analysis;
    }

    private void createAlertsForTriggeredRules(DomainFraudAnalysis saved,
                                                List<DomainFraudRule> activeRules,
                                                EngineResult result) {
        for (DomainFraudAnalysis.RuleEvaluationResult triggered : result.rulesTriggered()) {
            activeRules.stream()
                    .filter(r -> r.getRuleCode().equals(triggered.getRuleCode()))
                    .findFirst()
                    .ifPresent(rule -> {
                        DomainFraudAlert alert = new DomainFraudAlert();
                        alert.setFraudAnalysisId(saved.getId());
                        alert.setFraudRuleId(rule.getId());
                        alert.setAlertStatus(AlertStatus.OPEN);
                        alert.setScoreAtTrigger(saved.getRiskScore());
                        alert.setTriggeredAt(OffsetDateTime.now());
                        alertRepository.save(alert);
                    });
        }
    }

    private void updateRiskProfile(DomainFraudAnalysis saved,
                                    Optional<DomainCustomerRiskProfile> existing) {
        DomainCustomerRiskProfile profile = existing.orElseGet(() -> {
            DomainCustomerRiskProfile p = new DomainCustomerRiskProfile();
            p.setCustomerId(saved.getCustomerId());
            p.setLifetimeRiskScore(BigDecimal.ZERO);
            p.setTotalAlerts(0);
            p.setFalsePositives(0);
            p.setBlacklisted(false);
            p.setAvgTransactionAmount(BigDecimal.ZERO);
            return p;
        });

        int newTotal = profile.getTotalAlerts() + (saved.getVerdict() != FraudVerdict.CLEARED ? 1 : 0);
        BigDecimal newLifetime = profile.getLifetimeRiskScore()
                .add(saved.getRiskScore())
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        profile.setTotalAlerts(newTotal);
        profile.setLifetimeRiskScore(newLifetime);
        profile.setLastAnalysisAt(saved.getCreatedAt());
        profile.setUpdatedAt(OffsetDateTime.now());

        if (saved.getVerdict() == FraudVerdict.BLOCKED) {
            profile.setBlacklisted(true);
        }

        riskProfileRepository.save(profile);
    }

    private Boolean isNewDevice(String deviceFingerprint,
                                  Optional<DomainCustomerRiskProfile> existing) {
        if (deviceFingerprint == null) return null;
        // Dans cette version, tout device est considéré nouveau si c'est le premier profil
        return existing.isEmpty();
    }
}
