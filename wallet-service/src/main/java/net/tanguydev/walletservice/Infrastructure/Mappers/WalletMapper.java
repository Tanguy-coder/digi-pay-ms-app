package net.tanguydev.walletservice.Infrastructure.Mappers;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Responses.WalletResponse;
import net.tanguydev.walletservice.Infrastructure.Models.Wallet;
import net.tanguydev.walletservice.Infrastructure.Requests.WalletRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface WalletMapper {

    DomainWallet toDomain(Wallet wallet);

    Wallet toJpa(DomainWallet wallet);

    List<DomainWallet> toDomainList(List<Wallet> wallets);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "walletNumber", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "frozenAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DomainWallet requestToDomain(WalletRequest request);

    @Mapping(target = "availableBalance", expression = "java(wallet.getAvailableBalance())")
    WalletResponse toResponse(DomainWallet wallet);

    List<WalletResponse> toResponseList(List<DomainWallet> wallets);
}
