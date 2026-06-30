package net.tanguydev.customerservice.Infrastructure.Config;

import net.tanguydev.customerservice.Infrastructure.Mappers.CustomerMapper;
import net.tanguydev.customerservice.Infrastructure.Presenters.CustomerPresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresentationConfig {
    @Bean
    public CustomerPresenter customerPresenter(CustomerMapper customerMapper) {
        return new CustomerPresenter(customerMapper);
    }
}
