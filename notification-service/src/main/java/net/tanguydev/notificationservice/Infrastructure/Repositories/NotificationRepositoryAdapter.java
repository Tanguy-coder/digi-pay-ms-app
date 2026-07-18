package net.tanguydev.notificationservice.Infrastructure.Repositories;

import net.tanguydev.notificationservice.Domain.Entities.DomainNotification;
import net.tanguydev.notificationservice.Domain.Ports.NotificationRepositoryInterface;
import net.tanguydev.notificationservice.Infrastructure.Mappers.NotificationMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryInterface {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository, NotificationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DomainNotification save(DomainNotification notification) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(notification)));
    }

    @Override
    public List<DomainNotification> findByWalletId(UUID walletId) {
        return mapper.toDomainList(jpaRepository.findByWalletId(walletId));
    }

    @Override
    public List<DomainNotification> findByPaymentId(UUID paymentId) {
        return mapper.toDomainList(jpaRepository.findByPaymentId(paymentId));
    }
}
