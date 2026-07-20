package net.tanguydev.settlementservice.Infrastructure.Config;

import net.tanguydev.settlementservice.Domain.Presenters.BatchPresenterInterface;
import net.tanguydev.settlementservice.Infrastructure.Presenters.BatchPresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresentationConfig {

    @Bean
    public BatchPresenterInterface batchPresenter() {
        return new BatchPresenter();
    }
}
