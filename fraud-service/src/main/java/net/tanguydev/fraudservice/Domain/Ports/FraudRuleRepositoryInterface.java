package net.tanguydev.fraudservice.Domain.Ports;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudRule;

import java.util.List;

public interface FraudRuleRepositoryInterface {

    List<DomainFraudRule> findAllActive();
}
