package net.tanguydev.fraudservice.Infrastructure.Mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.fraudservice.Domain.Entities.DomainCustomerRiskProfile;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAlert;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis.RuleEvaluationResult;
import net.tanguydev.fraudservice.Domain.Entities.DomainFraudRule;
import net.tanguydev.fraudservice.Domain.Responses.FraudAnalysisResponse;
import net.tanguydev.fraudservice.Infrastructure.Models.CustomerRiskProfile;
import net.tanguydev.fraudservice.Infrastructure.Models.FraudAlert;
import net.tanguydev.fraudservice.Infrastructure.Models.FraudAnalysis;
import net.tanguydev.fraudservice.Infrastructure.Models.FraudRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface FraudMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ── FraudRule ──────────────────────────────────────────────────────────────

    DomainFraudRule toRuleDomain(FraudRule jpa);

    List<DomainFraudRule> toRuleDomainList(List<FraudRule> list);

    // ── FraudAnalysis ──────────────────────────────────────────────────────────

    @Mapping(target = "rulesTriggered", source = "rulesTriggered", qualifiedByName = "jsonToRules")
    DomainFraudAnalysis toAnalysisDomain(FraudAnalysis jpa);

    @Mapping(target = "rulesTriggered", source = "rulesTriggered", qualifiedByName = "rulesToJson")
    FraudAnalysis toAnalysisJpa(DomainFraudAnalysis domain);

    List<DomainFraudAnalysis> toAnalysisDomainList(List<FraudAnalysis> list);

    FraudAnalysisResponse toResponse(DomainFraudAnalysis domain);

    List<FraudAnalysisResponse> toResponseList(List<DomainFraudAnalysis> list);

    // ── FraudAlert ─────────────────────────────────────────────────────────────

    DomainFraudAlert toAlertDomain(FraudAlert jpa);

    FraudAlert toAlertJpa(DomainFraudAlert domain);

    List<DomainFraudAlert> toAlertDomainList(List<FraudAlert> list);

    // ── CustomerRiskProfile ────────────────────────────────────────────────────

    DomainCustomerRiskProfile toProfileDomain(CustomerRiskProfile jpa);

    CustomerRiskProfile toProfileJpa(DomainCustomerRiskProfile domain);

    // ── Conversion JSON ↔ List<RuleEvaluationResult> ──────────────────────────

    @Named("rulesToJson")
    static String rulesToJson(List<RuleEvaluationResult> rules) {
        if (rules == null || rules.isEmpty()) return "[]";
        try {
            return OBJECT_MAPPER.writeValueAsString(
                    rules.stream().map(r -> Map.of(
                            "ruleCode", r.getRuleCode(),
                            "scoreContribution", r.getScoreContribution().toPlainString(),
                            "reason", r.getReason() != null ? r.getReason() : ""
                    )).toList()
            );
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @Named("jsonToRules")
    static List<RuleEvaluationResult> jsonToRules(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            List<Map<String, String>> raw = OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
            return raw.stream().map(m -> new RuleEvaluationResult(
                    m.get("ruleCode"),
                    new BigDecimal(m.get("scoreContribution")),
                    m.get("reason")
            )).toList();
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
