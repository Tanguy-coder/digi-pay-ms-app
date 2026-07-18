package net.tanguydev.settlementservice.Infrastructure.Config;

import net.tanguydev.settlementservice.Domain.Presenters.SettlementPresenterInterface;
import net.tanguydev.settlementservice.Infrastructure.Presenters.SettlementPresenter;
import net.tanguydev.settlementservice.Infrastructure.Mappers.SettlementMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresentationConfig {

    @Bean
    public SettlementPresenterInterface settlementPresenter(SettlementMapper mapper) {
        return new SettlementPresenter(mapper);
    }
}
