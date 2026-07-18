package net.tanguydev.fraudservice.Infrastructure.Adapters;

import net.tanguydev.fraudservice.Domain.Ports.VelocityCounterInterface;
import net.tanguydev.fraudservice.Infrastructure.Repositories.FraudAnalysisJpaRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class InMemoryVelocityCounter implements VelocityCounterInterface {

    private final FraudAnalysisJpaRepository jpa;

    public InMemoryVelocityCounter(FraudAnalysisJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public int countLastMinute(UUID senderWalletId) {
        // Compte les analyses créées dans la dernière minute pour ce paymentId
        // Approximation : on compte par customer_id via les analyses récentes
        OffsetDateTime since = OffsetDateTime.now().minusMinutes(1);
        return (int) jpa.findAll().stream()
                .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(since))
                .count();
    }

    @Override
    public int countLastHour(UUID senderWalletId) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(1);
        return (int) jpa.findAll().stream()
                .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(since))
                .count();
    }
}
