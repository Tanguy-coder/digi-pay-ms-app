package net.tanguydev.fraudservice.Infrastructure.Presenters;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Presenters.FraudAnalysisPresenterInterface;
import net.tanguydev.fraudservice.Domain.Responses.FraudAnalysisResponse;
import net.tanguydev.fraudservice.Infrastructure.Mappers.FraudMapper;

import java.util.List;

public class FraudAnalysisPresenter implements FraudAnalysisPresenterInterface {

    private final FraudMapper mapper;

    public FraudAnalysisPresenter(FraudMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public FraudAnalysisResponse present(DomainFraudAnalysis analysis) {
        return mapper.toResponse(analysis);
    }

    @Override
    public List<FraudAnalysisResponse> present(List<DomainFraudAnalysis> analyses) {
        return mapper.toResponseList(analyses);
    }
}
