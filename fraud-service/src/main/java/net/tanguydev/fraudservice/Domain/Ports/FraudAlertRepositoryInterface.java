package net.tanguydev.fraudservice.Domain.Ports;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAlert;

import java.util.List;
import java.util.UUID;

public interface FraudAlertRepositoryInterface {

    DomainFraudAlert save(DomainFraudAlert alert);

    List<DomainFraudAlert> findByFraudAnalysisId(UUID fraudAnalysisId);
}
