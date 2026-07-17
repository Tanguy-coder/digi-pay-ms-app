package net.tanguydev.paymentservice.Infrastructure.Config;

import net.tanguydev.paymentservice.Domain.Gateways.SagaStepRepositoryInterface;
import net.tanguydev.paymentservice.Domain.Ports.IdempotencyStoreInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentEventPublisherInterface;
import net.tanguydev.paymentservice.Domain.Ports.PaymentServiceInterface;
import net.tanguydev.paymentservice.Domain.Ports.WalletCommandPublisherInterface;
import net.tanguydev.paymentservice.Domain.UseCases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public PaymentSagaOrchestrator paymentSagaOrchestrator(PaymentServiceInterface paymentService,
                                                           SagaStepRepositoryInterface sagaStepRepository,
                                                           WalletCommandPublisherInterface commandPublisher,
                                                           PaymentEventPublisherInterface eventPublisher) {
        return new PaymentSagaOrchestrator(paymentService, sagaStepRepository, commandPublisher, eventPublisher);
    }

    @Bean
    public InitiatePaymentUseCase initiatePaymentUseCase(PaymentServiceInterface paymentService,
                                                         PaymentEventPublisherInterface eventPublisher,
                                                         IdempotencyStoreInterface idempotencyStore,
                                                         PaymentSagaOrchestratorInterface sagaOrchestrator) {
        return new InitiatePaymentUseCase(paymentService, eventPublisher, idempotencyStore, sagaOrchestrator);
    }

    @Bean
    public FindPaymentByIdUseCase findPaymentByIdUseCase(PaymentServiceInterface paymentService) {
        return new FindPaymentByIdUseCase(paymentService);
    }

    @Bean
    public FindPaymentsByWalletUseCase findPaymentsByWalletUseCase(PaymentServiceInterface paymentService) {
        return new FindPaymentsByWalletUseCase(paymentService);
    }
}
