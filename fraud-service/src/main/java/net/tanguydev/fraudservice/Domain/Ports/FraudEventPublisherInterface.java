package net.tanguydev.fraudservice.Domain.Ports;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;

public interface FraudEventPublisherInterface {

    void publish(DomainFraudAnalysis analysis);
}
