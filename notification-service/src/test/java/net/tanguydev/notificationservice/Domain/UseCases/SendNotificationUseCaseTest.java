package net.tanguydev.notificationservice.Domain.UseCases;

import net.tanguydev.notificationservice.Domain.Entities.DomainNotification;
import net.tanguydev.notificationservice.Domain.Enums.NotificationStatus;
import net.tanguydev.notificationservice.Domain.Enums.NotificationType;
import net.tanguydev.notificationservice.Domain.Ports.NotificationRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationUseCaseTest {

    @Mock
    private NotificationRepositoryInterface repository;

    private SendNotificationUseCaseInterface useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendNotificationUseCase(repository);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private SendNotificationCommand command(NotificationType type) {
        SendNotificationCommand cmd = new SendNotificationCommand();
        cmd.setPaymentId(UUID.randomUUID());
        cmd.setWalletId(UUID.randomUUID());
        cmd.setType(type);
        cmd.setAmount(new BigDecimal("250.00"));
        cmd.setCurrency("EUR");
        return cmd;
    }

    @Test
    void execute_alwaysSavesNotification() {
        useCase.execute(command(NotificationType.PAYMENT_INITIATED));
        verify(repository).save(any(DomainNotification.class));
    }

    @Test
    void execute_setsStatusToSent() {
        ArgumentCaptor<DomainNotification> captor = ArgumentCaptor.forClass(DomainNotification.class);
        useCase.execute(command(NotificationType.PAYMENT_COMPLETED));
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void execute_paymentInitiated_buildsCorrectMessage() {
        ArgumentCaptor<DomainNotification> captor = ArgumentCaptor.forClass(DomainNotification.class);
        useCase.execute(command(NotificationType.PAYMENT_INITIATED));
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("initiated");
        assertThat(captor.getValue().getMessage()).contains("250.00");
    }

    @Test
    void execute_paymentCompleted_buildsCorrectMessage() {
        ArgumentCaptor<DomainNotification> captor = ArgumentCaptor.forClass(DomainNotification.class);
        useCase.execute(command(NotificationType.PAYMENT_COMPLETED));
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("completed");
    }

    @Test
    void execute_fraudBlocked_buildsCorrectMessage() {
        ArgumentCaptor<DomainNotification> captor = ArgumentCaptor.forClass(DomainNotification.class);
        useCase.execute(command(NotificationType.FRAUD_BLOCKED));
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("blocked");
    }

    @Test
    void execute_populatesAllFields() {
        SendNotificationCommand cmd = command(NotificationType.PAYMENT_FAILED);
        ArgumentCaptor<DomainNotification> captor = ArgumentCaptor.forClass(DomainNotification.class);
        useCase.execute(cmd);
        verify(repository).save(captor.capture());
        DomainNotification saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getWalletId()).isEqualTo(cmd.getWalletId());
        assertThat(saved.getPaymentId()).isEqualTo(cmd.getPaymentId());
        assertThat(saved.getType()).isEqualTo(NotificationType.PAYMENT_FAILED);
        assertThat(saved.getAmount()).isEqualByComparingTo("250.00");
        assertThat(saved.getCurrency()).isEqualTo("EUR");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
