package net.tanguydev.fraudservice.Infrastructure.Config;

import net.tanguydev.fraudservice.Domain.Ports.CustomerRiskProfileRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudAlertRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudAnalysisRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudEventPublisherInterface;
import net.tanguydev.fraudservice.Domain.Ports.FraudRuleRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Ports.VelocityCounterInterface;
import net.tanguydev.fraudservice.Domain.Services.FraudRulesEngine;
import net.tanguydev.fraudservice.Domain.UseCases.AnalyzePaymentUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public FraudRulesEngine fraudRulesEngine() {
        return new FraudRulesEngine();
    }

    @Bean
    public AnalyzePaymentUseCase analyzePaymentUseCase(
            FraudRuleRepositoryInterface ruleRepository,
            FraudAnalysisRepositoryInterface analysisRepository,
            FraudAlertRepositoryInterface alertRepository,
            CustomerRiskProfileRepositoryInterface riskProfileRepository,
            FraudEventPublisherInterface eventPublisher,
            VelocityCounterInterface velocityCounter,
            FraudRulesEngine rulesEngine) {
        return new AnalyzePaymentUseCase(
                ruleRepository,
                analysisRepository,
                alertRepository,
                riskProfileRepository,
                eventPublisher,
                velocityCounter,
                rulesEngine
        );
    }
}
