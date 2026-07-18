package net.tanguydev.paymentservice.Domain.Enums;

public enum SagaStepName {
    FRAUD_CHECK, DEBIT_SENDER, CREDIT_RECEIVER, NOTIFY, SETTLE
}
