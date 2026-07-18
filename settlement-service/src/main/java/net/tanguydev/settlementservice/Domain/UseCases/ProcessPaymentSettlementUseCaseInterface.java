package net.tanguydev.settlementservice.Domain.UseCases;

public interface ProcessPaymentSettlementUseCaseInterface {
    void execute(ProcessPaymentSettlementCommand command);
}
