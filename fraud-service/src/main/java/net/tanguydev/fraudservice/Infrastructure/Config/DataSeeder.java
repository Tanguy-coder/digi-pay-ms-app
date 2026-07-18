package net.tanguydev.fraudservice.Infrastructure.Config;

import net.tanguydev.fraudservice.Domain.Enums.RuleAction;
import net.tanguydev.fraudservice.Domain.Enums.RulePriority;
import net.tanguydev.fraudservice.Domain.Enums.RuleType;
import net.tanguydev.fraudservice.Infrastructure.Models.FraudRule;
import net.tanguydev.fraudservice.Infrastructure.Repositories.FraudRuleJpaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private final FraudRuleJpaRepository ruleRepository;

    public DataSeeder(FraudRuleJpaRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (ruleRepository.count() > 0) return;

        ruleRepository.saveAll(List.of(
                FraudRule.builder()
                        .ruleCode("HIGH_AMOUNT")
                        .description("Block payments above 10 000 units")
                        .ruleType(RuleType.AMOUNT)
                        .thresholdValue(new BigDecimal("10000.00"))
                        .scoreWeight(new BigDecimal("85.00"))
                        .action(RuleAction.BLOCK)
                        .priority(RulePriority.CRITICAL)
                        .active(true)
                        .build(),

                FraudRule.builder()
                        .ruleCode("VELOCITY_1MIN")
                        .description("Flag when more than 3 transactions in 1 minute")
                        .ruleType(RuleType.VELOCITY)
                        .thresholdValue(new BigDecimal("3"))
                        .scoreWeight(new BigDecimal("40.00"))
                        .action(RuleAction.REVIEW)
                        .priority(RulePriority.HIGH)
                        .active(true)
                        .build(),

                FraudRule.builder()
                        .ruleCode("VELOCITY_1H")
                        .description("Flag when more than 10 transactions in 1 hour")
                        .ruleType(RuleType.VELOCITY)
                        .thresholdValue(new BigDecimal("10"))
                        .scoreWeight(new BigDecimal("25.00"))
                        .action(RuleAction.FLAG)
                        .priority(RulePriority.MEDIUM)
                        .active(true)
                        .build(),

                FraudRule.builder()
                        .ruleCode("RISKY_COUNTRY_KP")
                        .description("Block transactions from North Korea")
                        .ruleType(RuleType.GEOLOCATION)
                        .thresholdValue(BigDecimal.ZERO)
                        .scoreWeight(new BigDecimal("90.00"))
                        .action(RuleAction.BLOCK)
                        .priority(RulePriority.CRITICAL)
                        .active(true)
                        .build(),

                FraudRule.builder()
                        .ruleCode("RISKY_COUNTRY_IR")
                        .description("Block transactions from Iran")
                        .ruleType(RuleType.GEOLOCATION)
                        .thresholdValue(BigDecimal.ZERO)
                        .scoreWeight(new BigDecimal("90.00"))
                        .action(RuleAction.BLOCK)
                        .priority(RulePriority.CRITICAL)
                        .active(true)
                        .build(),

                FraudRule.builder()
                        .ruleCode("NEW_DEVICE")
                        .description("Review payments from an unrecognised device")
                        .ruleType(RuleType.DEVICE)
                        .thresholdValue(BigDecimal.ZERO)
                        .scoreWeight(new BigDecimal("20.00"))
                        .action(RuleAction.CHALLENGE_OTP)
                        .priority(RulePriority.MEDIUM)
                        .active(true)
                        .build(),

                FraudRule.builder()
                        .ruleCode("SUSPICIOUS_HOUR")
                        .description("Flag transactions between 00:00 and 05:00 UTC")
                        .ruleType(RuleType.BEHAVIORAL)
                        .thresholdValue(BigDecimal.ZERO)
                        .scoreWeight(new BigDecimal("15.00"))
                        .action(RuleAction.FLAG)
                        .priority(RulePriority.LOW)
                        .active(true)
                        .build()
        ));
    }
}
