package net.tanguydev.notificationservice.Infrastructure.Config;

import net.tanguydev.notificationservice.Infrastructure.Mappers.NotificationMapper;
import net.tanguydev.notificationservice.Infrastructure.Presenters.NotificationPresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PresentationConfig {

    @Bean
    public NotificationPresenter notificationPresenter(NotificationMapper mapper) {
        return new NotificationPresenter(mapper);
    }
}
