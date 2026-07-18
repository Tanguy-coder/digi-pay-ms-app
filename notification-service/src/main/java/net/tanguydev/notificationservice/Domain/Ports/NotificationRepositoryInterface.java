package net.tanguydev.notificationservice.Domain.Ports;

import net.tanguydev.notificationservice.Domain.Entities.DomainNotification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryInterface {
    DomainNotification save(DomainNotification notification);
    List<DomainNotification> findByWalletId(UUID walletId);
    List<DomainNotification> findByPaymentId(UUID paymentId);
}
