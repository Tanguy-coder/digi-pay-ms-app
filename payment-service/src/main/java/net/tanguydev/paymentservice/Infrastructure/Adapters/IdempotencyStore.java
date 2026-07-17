package net.tanguydev.paymentservice.Infrastructure.Adapters;

import net.tanguydev.paymentservice.Domain.Entities.DomainIdempotencyKey;
import net.tanguydev.paymentservice.Domain.Ports.IdempotencyStoreInterface;
import net.tanguydev.paymentservice.Infrastructure.Models.IdempotencyKey;
import net.tanguydev.paymentservice.Infrastructure.Repositories.IdempotencyKeyJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdempotencyStore implements IdempotencyStoreInterface {

    private final IdempotencyKeyJpaRepository jpaRepository;

    public IdempotencyStore(IdempotencyKeyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void store(DomainIdempotencyKey entry) {
        IdempotencyKey jpa = new IdempotencyKey();
        jpa.setKey(entry.getKey());
        jpa.setResponseStatus(entry.getResponseStatus());
        jpa.setResponseBody(entry.getResponseBody());
        jpa.setExpiresAt(entry.getExpiresAt());
        jpaRepository.save(jpa);
    }

    @Override
    public Optional<DomainIdempotencyKey> find(String key) {
        return jpaRepository.findById(key).map(jpa -> {
            DomainIdempotencyKey domain = new DomainIdempotencyKey();
            domain.setKey(jpa.getKey());
            domain.setResponseStatus(jpa.getResponseStatus());
            domain.setResponseBody(jpa.getResponseBody());
            domain.setExpiresAt(jpa.getExpiresAt());
            domain.setCreatedAt(jpa.getCreatedAt());
            return domain;
        });
    }

    @Override
    public boolean exists(String key) {
        return jpaRepository.existsById(key);
    }
}
