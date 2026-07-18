package net.tanguydev.notificationservice.Infrastructure.Repositories;

import net.tanguydev.notificationservice.Infrastructure.Models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByWalletId(UUID walletId);
    List<Notification> findByPaymentId(UUID paymentId);
}
