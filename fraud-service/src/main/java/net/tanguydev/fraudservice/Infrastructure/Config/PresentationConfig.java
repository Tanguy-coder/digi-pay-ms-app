package net.tanguydev.fraudservice.Infrastructure.Config;

import net.tanguydev.fraudservice.Infrastructure.Mappers.FraudMapper;
import net.tanguydev.fraudservice.Infrastructure.Presenters.FraudAnalysisPresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresentationConfig {

    @Bean
    public FraudAnalysisPresenter fraudAnalysisPresenter(FraudMapper mapper) {
        return new FraudAnalysisPresenter(mapper);
    }
}
