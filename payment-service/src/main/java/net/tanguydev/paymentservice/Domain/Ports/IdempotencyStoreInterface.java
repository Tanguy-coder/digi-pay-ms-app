package net.tanguydev.paymentservice.Domain.Ports;

import net.tanguydev.paymentservice.Domain.Entities.DomainIdempotencyKey;

import java.util.Optional;

public interface IdempotencyStoreInterface {

    void store(DomainIdempotencyKey entry);

    Optional<DomainIdempotencyKey> find(String key);

    boolean exists(String key);
}
