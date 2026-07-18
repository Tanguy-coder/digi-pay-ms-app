package net.tanguydev.fraudservice.Infrastructure.Repositories;

import net.tanguydev.fraudservice.Infrastructure.Models.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FraudAlertJpaRepository extends JpaRepository<FraudAlert, UUID> {

    List<FraudAlert> findByFraudAnalysisId(UUID fraudAnalysisId);
}
