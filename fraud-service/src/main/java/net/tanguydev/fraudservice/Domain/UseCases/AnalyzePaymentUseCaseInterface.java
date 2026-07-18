package net.tanguydev.fraudservice.Domain.UseCases;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;

public interface AnalyzePaymentUseCaseInterface {

    DomainFraudAnalysis execute(AnalyzePaymentCommand command);
}
