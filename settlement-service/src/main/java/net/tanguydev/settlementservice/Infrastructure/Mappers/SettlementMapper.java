package net.tanguydev.settlementservice.Infrastructure.Mappers;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Responses.SettlementEntryResponse;
import net.tanguydev.settlementservice.Domain.Responses.SettlementResponse;
import net.tanguydev.settlementservice.Infrastructure.Models.Settlement;
import net.tanguydev.settlementservice.Infrastructure.Models.SettlementEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SettlementMapper {

    Settlement toJpa(DomainSettlement domain);

    @Mapping(target = "entries", ignore = true)
    DomainSettlement toDomain(Settlement jpa);

    SettlementEntry toEntryJpa(DomainSettlementEntry domain);

    DomainSettlementEntry toEntryDomain(SettlementEntry jpa);

    List<DomainSettlementEntry> toEntryDomainList(List<SettlementEntry> jpaList);

    SettlementResponse toResponse(DomainSettlement domain);

    SettlementEntryResponse toEntryResponse(DomainSettlementEntry domain);

    List<SettlementEntryResponse> toEntryResponseList(List<DomainSettlementEntry> domainList);
}
