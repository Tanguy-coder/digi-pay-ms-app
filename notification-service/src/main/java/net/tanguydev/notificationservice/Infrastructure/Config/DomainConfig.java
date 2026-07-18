package net.tanguydev.notificationservice.Infrastructure.Config;

import net.tanguydev.notificationservice.Domain.Ports.NotificationRepositoryInterface;
import net.tanguydev.notificationservice.Domain.UseCases.SendNotificationUseCase;
import net.tanguydev.notificationservice.Domain.UseCases.SendNotificationUseCaseInterface;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public SendNotificationUseCaseInterface sendNotificationUseCase(NotificationRepositoryInterface repository) {
        return new SendNotificationUseCase(repository);
    }
}
