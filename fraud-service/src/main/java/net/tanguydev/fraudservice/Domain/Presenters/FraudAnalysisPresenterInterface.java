package net.tanguydev.fraudservice.Domain.Presenters;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Responses.FraudAnalysisResponse;

import java.util.List;

public interface FraudAnalysisPresenterInterface {

    FraudAnalysisResponse present(DomainFraudAnalysis analysis);

    List<FraudAnalysisResponse> present(List<DomainFraudAnalysis> analyses);
}
