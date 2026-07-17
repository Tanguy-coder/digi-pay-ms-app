package net.tanguydev.paymentservice.Domain.Gateways;

import net.tanguydev.paymentservice.Domain.Entities.DomainOutboxEvent;

import java.util.List;

public interface OutboxEventRepositoryInterface {

    DomainOutboxEvent save(DomainOutboxEvent event);

    List<DomainOutboxEvent> findUnpublished();

    void markPublished(java.util.UUID id);
}
