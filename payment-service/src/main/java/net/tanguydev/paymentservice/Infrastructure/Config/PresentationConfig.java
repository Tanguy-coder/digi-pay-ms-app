package net.tanguydev.paymentservice.Infrastructure.Config;

import net.tanguydev.paymentservice.Infrastructure.Mappers.PaymentMapper;
import net.tanguydev.paymentservice.Infrastructure.Presenters.PaymentPresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresentationConfig {

    @Bean
    public PaymentPresenter paymentPresenter(PaymentMapper mapper) {
        return new PaymentPresenter(mapper);
    }
}
