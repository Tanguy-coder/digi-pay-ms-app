package net.tanguydev.paymentservice.Infrastructure.Mappers;

import net.tanguydev.paymentservice.Domain.Entities.DomainOutboxEvent;
import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Entities.DomainSagaStep;
import net.tanguydev.paymentservice.Domain.Responses.PaymentResponse;
import net.tanguydev.paymentservice.Infrastructure.Models.OutboxEvent;
import net.tanguydev.paymentservice.Infrastructure.Models.Payment;
import net.tanguydev.paymentservice.Infrastructure.Models.SagaStep;
import net.tanguydev.paymentservice.Infrastructure.Requests.InitiatePaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PaymentMapper {

    DomainPayment toDomain(Payment payment);

    Payment toJpa(DomainPayment payment);

    List<DomainPayment> toDomainList(List<Payment> payments);

    DomainSagaStep toDomain(SagaStep step);

    SagaStep toJpa(DomainSagaStep step);

    DomainOutboxEvent toDomain(OutboxEvent event);

    OutboxEvent toJpa(DomainOutboxEvent event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentReference", ignore = true)
    @Mapping(target = "feeAmount", ignore = true)
    @Mapping(target = "exchangeRate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "initiatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    DomainPayment requestToDomain(InitiatePaymentRequest request);

    @Mapping(target = "netAmount", expression = "java(payment.getNetAmount())")
    PaymentResponse toResponse(DomainPayment payment);

    List<PaymentResponse> toResponseList(List<DomainPayment> payments);
}
