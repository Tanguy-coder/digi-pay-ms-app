package net.tanguydev.settlementservice.Infrastructure.Presenters;

import net.tanguydev.settlementservice.Domain.Entities.DomainNetPosition;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Presenters.BatchPresenterInterface;
import net.tanguydev.settlementservice.Domain.Responses.BatchResponse;
import net.tanguydev.settlementservice.Domain.Responses.EntryResponse;
import net.tanguydev.settlementservice.Domain.Responses.NetPositionResponse;

import java.util.List;

public class BatchPresenter implements BatchPresenterInterface {

    @Override
    public BatchResponse present(DomainSettlementBatch batch) {
        BatchResponse response = new BatchResponse();
        response.setId(batch.getId());
        response.setReference(batch.getReference());
        response.setStatus(batch.getStatus().name());
        response.setCycle(batch.getCycle().name());
        response.setCurrency(batch.getCurrency());
        response.setTotalEntries(batch.getTotalEntries());
        response.setTotalAmount(batch.getTotalAmount());
        response.setOpenedAt(batch.getOpenedAt());
        response.setClosedAt(batch.getClosedAt());
        response.setSettledAt(batch.getSettledAt());

        if (batch.getEntries() != null) {
            response.setEntries(batch.getEntries().stream().map(this::toEntryResponse).toList());
        }
        if (batch.getPositions() != null) {
            response.setPositions(batch.getPositions().stream().map(this::toPositionResponse).toList());
        }

        return response;
    }

    private EntryResponse toEntryResponse(DomainSettlementEntry entry) {
        EntryResponse response = new EntryResponse();
        response.setId(entry.getId());
        response.setPaymentId(entry.getPaymentId());
        response.setPaymentReference(entry.getPaymentReference());
        response.setSenderWalletId(entry.getSenderWalletId());
        response.setReceiverWalletId(entry.getReceiverWalletId());
        response.setAmount(entry.getAmount());
        response.setCurrency(entry.getCurrency());
        response.setCapturedAt(entry.getCapturedAt());
        return response;
    }

    private NetPositionResponse toPositionResponse(DomainNetPosition position) {
        NetPositionResponse response = new NetPositionResponse();
        response.setId(position.getId());
        response.setWalletId(position.getWalletId());
        response.setGrossDebit(position.getGrossDebit());
        response.setGrossCredit(position.getGrossCredit());
        response.setNetAmount(position.getNetAmount());
        response.setStatus(position.getStatus().name());
        return response;
    }
}
