package net.tanguydev.fraudservice.Domain.UseCases;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudRule;
import net.tanguydev.fraudservice.Domain.Enums.FraudVerdict;
import net.tanguydev.fraudservice.Domain.Enums.RuleAction;
import net.tanguydev.fraudservice.Domain.Enums.RuleType;
import net.tanguydev.fraudservice.Domain.Ports.CustomerRiskProfileRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudAlertRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudAnalysisRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudEventPublisherInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudRuleRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.VelocityCounterInterface;
import net.tanguydev.fraudservice.Domain.Services.FraudRulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyzePaymentUseCaseTest {

    @Mock private FraudRuleRepositoryInterface ruleRepository;
    @Mock private FraudAnalysisRepositoryInterface analysisRepository;
    @Mock private FraudAlertRepositoryInterface alertRepository;
    @Mock private CustomerRiskProfileRepositoryInterface riskProfileRepository;
    @Mock private FraudEventPublisherInterface eventPublisher;
    @Mock private VelocityCounterInterface velocityCounter;

    private AnalyzePaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AnalyzePaymentUseCase(
                ruleRepository, analysisRepository, alertRepository,
                riskProfileRepository, eventPublisher, velocityCounter,
                new FraudRulesEngine()
        );
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private AnalyzePaymentCommand command(BigDecimal amount, String country) {
        AnalyzePaymentCommand cmd = new AnalyzePaymentCommand();
        cmd.setPaymentId(UUID.randomUUID());
        cmd.setCustomerId(UUID.randomUUID());
        cmd.setSenderWalletId(UUID.randomUUID());
        cmd.setAmount(amount);
        cmd.setCurrency("EUR");
        cmd.setCountryCode(country);
        cmd.setIpAddress("1.2.3.4");
        cmd.setDeviceFingerprint("fp-test");
        return cmd;
    }

    private DomainFraudRule blockRule(String code, RuleType type, BigDecimal threshold, BigDecimal weight) {
        DomainFraudRule r = new DomainFraudRule();
        r.setId(UUID.randomUUID());
        r.setRuleCode(code);
        r.setRuleType(type);
        r.setThresholdValue(threshold);
        r.setScoreWeight(weight);
        r.setAction(RuleAction.BLOCK);
        r.setActive(true);
        return r;
    }

    private DomainFraudAnalysis savedAnalysis(DomainFraudAnalysis analysis) {
        analysis.setId(UUID.randomUUID());
        return analysis;
    }

    // ── cleared path ───────────────────────────────────────────────────────────

    @Test
    void normalPayment_cleared_noAlerts() {
        AnalyzePaymentCommand cmd = command(new BigDecimal("100"), "FR");

        when(ruleRepository.findAllActive()).thenReturn(List.of());
        when(velocityCounter.countLastMinute(any())).thenReturn(0);
        when(velocityCounter.countLastHour(any())).thenReturn(0);
        when(riskProfileRepository.findByCustomerId(any())).thenReturn(Optional.empty());
        when(analysisRepository.save(any())).thenAnswer(inv -> savedAnalysis(inv.getArgument(0)));

        DomainFraudAnalysis result = useCase.execute(cmd);

        assertThat(result.getVerdict()).isEqualTo(FraudVerdict.CLEARED);
        verify(alertRepository, never()).save(any());
        verify(eventPublisher).publish(result);
    }

    // ── blocked path ───────────────────────────────────────────────────────────

    @Test
    void highAmountPayment_blocked_alertCreated() {
        AnalyzePaymentCommand cmd = command(new BigDecimal("15000"), "FR");
        DomainFraudRule rule = blockRule("HIGH_AMOUNT", RuleType.AMOUNT,
                new BigDecimal("10000"), new BigDecimal("85"));

        when(ruleRepository.findAllActive()).thenReturn(List.of(rule));
        when(velocityCounter.countLastMinute(any())).thenReturn(0);
        when(velocityCounter.countLastHour(any())).thenReturn(0);
        when(riskProfileRepository.findByCustomerId(any())).thenReturn(Optional.empty());
        when(analysisRepository.save(any())).thenAnswer(inv -> savedAnalysis(inv.getArgument(0)));

        DomainFraudAnalysis result = useCase.execute(cmd);

        assertThat(result.getVerdict()).isEqualTo(FraudVerdict.BLOCKED);
        verify(alertRepository).save(any());
        verify(eventPublisher).publish(result);
    }

    @Test
    void riskyCountryPayment_blocked() {
        AnalyzePaymentCommand cmd = command(new BigDecimal("10"), "KP");
        DomainFraudRule rule = blockRule("RISKY_COUNTRY_KP", RuleType.GEOLOCATION,
                BigDecimal.ZERO, new BigDecimal("90"));

        when(ruleRepository.findAllActive()).thenReturn(List.of(rule));
        when(velocityCounter.countLastMinute(any())).thenReturn(0);
        when(velocityCounter.countLastHour(any())).thenReturn(0);
        when(riskProfileRepository.findByCustomerId(any())).thenReturn(Optional.empty());
        when(analysisRepository.save(any())).thenAnswer(inv -> savedAnalysis(inv.getArgument(0)));

        DomainFraudAnalysis result = useCase.execute(cmd);

        assertThat(result.getVerdict()).isEqualTo(FraudVerdict.BLOCKED);
    }

    // ── side effects ───────────────────────────────────────────────────────────

    @Test
    void execute_alwaysSavesAnalysisAndPublishesEvent() {
        AnalyzePaymentCommand cmd = command(new BigDecimal("200"), "FR");

        when(ruleRepository.findAllActive()).thenReturn(List.of());
        when(velocityCounter.countLastMinute(any())).thenReturn(0);
        when(velocityCounter.countLastHour(any())).thenReturn(0);
        when(riskProfileRepository.findByCustomerId(any())).thenReturn(Optional.empty());
        when(analysisRepository.save(any())).thenAnswer(inv -> savedAnalysis(inv.getArgument(0)));

        useCase.execute(cmd);

        verify(analysisRepository).save(any());
        verify(riskProfileRepository).save(any());
        verify(eventPublisher).publish(any());
    }

    @Test
    void execute_populatesAnalysisFields() {
        AnalyzePaymentCommand cmd = command(new BigDecimal("500"), "DE");

        when(ruleRepository.findAllActive()).thenReturn(List.of());
        when(velocityCounter.countLastMinute(any())).thenReturn(1);
        when(velocityCounter.countLastHour(any())).thenReturn(4);
        when(riskProfileRepository.findByCustomerId(any())).thenReturn(Optional.empty());
        when(analysisRepository.save(any())).thenAnswer(inv -> savedAnalysis(inv.getArgument(0)));

        DomainFraudAnalysis result = useCase.execute(cmd);

        assertThat(result.getPaymentId()).isEqualTo(cmd.getPaymentId());
        assertThat(result.getCustomerId()).isEqualTo(cmd.getCustomerId());
        assertThat(result.getCountryCode()).isEqualTo("DE");
        assertThat(result.getVelocity1min()).isEqualTo(1);
        assertThat(result.getVelocity1h()).isEqualTo(4);
        assertThat(result.getCreatedAt()).isNotNull();
    }
}
