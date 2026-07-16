package net.tanguydev.customerservice.Infrastructure.Config;

import net.tanguydev.customerservice.Domain.Ports.CustomerEventPublisherInterface;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.UseCases.CreateCustomerUseCase;
import net.tanguydev.customerservice.Domain.UseCases.FindCustomerByIdUseCase;
import net.tanguydev.customerservice.Domain.UseCases.ListCustomerUseCase;
import net.tanguydev.customerservice.Domain.UseCases.UpdateCustomerUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {
    @Bean
    public CreateCustomerUseCase createCustomerUseCase(CustomerServiceInterface customerService,
                                                       CustomerEventPublisherInterface eventPublisher) {
        return new CreateCustomerUseCase(customerService, eventPublisher);
    }

    @Bean
    public ListCustomerUseCase listCustomerUseCase(CustomerServiceInterface customerService) {
        return new ListCustomerUseCase(customerService);
    }

    @Bean
    public FindCustomerByIdUseCase findCustomerByIdUseCase(CustomerServiceInterface customerService) {
        return new FindCustomerByIdUseCase(customerService);
    }

    @Bean
    public UpdateCustomerUseCase updateCustomerUseCase(CustomerServiceInterface customerService) {
        return new UpdateCustomerUseCase(customerService);
    }
}
