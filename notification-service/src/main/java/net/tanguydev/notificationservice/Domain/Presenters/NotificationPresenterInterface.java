package net.tanguydev.notificationservice.Domain.Presenters;

import net.tanguydev.notificationservice.Domain.Entities.DomainNotification;
import net.tanguydev.notificationservice.Domain.Responses.NotificationResponse;

import java.util.List;

public interface NotificationPresenterInterface {
    NotificationResponse present(DomainNotification notification);
    List<NotificationResponse> present(List<DomainNotification> notifications);
}
