package net.tanguydev.notificationservice.Domain.UseCases;

public interface SendNotificationUseCaseInterface {
    void execute(SendNotificationCommand command);
}
