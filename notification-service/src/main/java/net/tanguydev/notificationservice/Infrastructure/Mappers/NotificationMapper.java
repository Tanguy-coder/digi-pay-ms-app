package net.tanguydev.notificationservice.Infrastructure.Mappers;

import net.tanguydev.notificationservice.Domain.Entities.DomainNotification;
import net.tanguydev.notificationservice.Domain.Responses.NotificationResponse;
import net.tanguydev.notificationservice.Infrastructure.Models.Notification;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    DomainNotification toDomain(Notification jpa);
    Notification toJpa(DomainNotification domain);
    NotificationResponse toResponse(DomainNotification domain);
    List<DomainNotification> toDomainList(List<Notification> list);
    List<NotificationResponse> toResponseList(List<DomainNotification> list);
}
