package net.tanguydev.notificationservice.Infrastructure.Presenters;

import net.tanguydev.notificationservice.Domain.Entities.DomainNotification;
import net.tanguydev.notificationservice.Domain.Presenters.NotificationPresenterInterface;
import net.tanguydev.notificationservice.Domain.Responses.NotificationResponse;
import net.tanguydev.notificationservice.Infrastructure.Mappers.NotificationMapper;

import java.util.List;

public class NotificationPresenter implements NotificationPresenterInterface {

    private final NotificationMapper mapper;

    public NotificationPresenter(NotificationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public NotificationResponse present(DomainNotification notification) {
        return mapper.toResponse(notification);
    }

    @Override
    public List<NotificationResponse> present(List<DomainNotification> notifications) {
        return mapper.toResponseList(notifications);
    }
}
