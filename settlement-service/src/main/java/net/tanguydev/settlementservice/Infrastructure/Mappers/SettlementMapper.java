package net.tanguydev.settlementservice.Infrastructure.Mappers;

import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Infrastructure.Models.NetPosition;
import net.tanguydev.settlementservice.Infrastructure.Models.SettlementBatch;
import net.tanguydev.settlementservice.Infrastructure.Models.SettlementEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SettlementMapper {

    @Mapping(target = "entries", ignore = true)
    @Mapping(target = "positions", ignore = true)
    DomainSettlementBatch toBatchDomain(SettlementBatch jpa);

    SettlementBatch toBatchJpa(DomainSettlementBatch domain);

    DomainSettlementEntry toEntryDomain(SettlementEntry jpa);

    SettlementEntry toEntryJpa(DomainSettlementEntry domain);

    DomainNetPosition toPositionDomain(NetPosition jpa);

    NetPosition toPositionJpa(DomainNetPosition domain);
}
