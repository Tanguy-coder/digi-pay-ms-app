package net.tanguydev.paymentservice.Infrastructure.Controllers;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Enums.PaymentType;
import net.tanguydev.paymentservice.Domain.Presenters.PaymentPresenterInterface;
import net.tanguydev.paymentservice.Domain.Responses.PaymentResponse;
import net.tanguydev.paymentservice.Domain.UseCases.FindPaymentByIdUseCaseInterface;
import net.tanguydev.paymentservice.Domain.UseCases.FindPaymentsByWalletUseCaseInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentQueryController.class)
class PaymentQueryControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private FindPaymentByIdUseCaseInterface findById;
    @MockitoBean private FindPaymentsByWalletUseCaseInterface findByWallet;
    @MockitoBean private PaymentPresenterInterface presenter;

    private static final UUID PAYMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SENDER_ID  = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID OTHER_ID   = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Test
    void show_shouldReturn200_whenFound() throws Exception {
        DomainPayment domain = buildDomain();
        PaymentResponse response = buildResponse();

        when(findById.execute(PAYMENT_ID)).thenReturn(Optional.of(domain));
        when(presenter.present(any(DomainPayment.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/" + PAYMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000));
    }

    @Test
    void show_shouldReturn404_whenNotFound() throws Exception {
        when(findById.execute(OTHER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/payments/" + OTHER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void showByWallet_shouldReturn200WithList() throws Exception {
        DomainPayment domain = buildDomain();
        PaymentResponse response = buildResponse();

        when(findByWallet.execute(SENDER_ID)).thenReturn(List.of(domain));
        when(presenter.present(List.of(domain))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/payments/wallet/" + SENDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].currency").value("XOF"));
    }

    private DomainPayment buildDomain() {
        DomainPayment p = new DomainPayment();
        p.setId(PAYMENT_ID);
        p.setSenderWalletId(SENDER_ID);
        p.setReceiverWalletId(UUID.fromString("00000000-0000-0000-0000-000000000020"));
        p.setAmount(new BigDecimal("5000"));
        p.setFeeAmount(BigDecimal.ZERO);
        p.setCurrency("XOF");
        p.setType(PaymentType.P2P);
        p.setStatus(PaymentStatus.INITIATED);
        p.setPaymentReference("PAY-20260717-12345");
        p.setIdempotencyKey("KEY-001");
        return p;
    }

    private PaymentResponse buildResponse() {
        PaymentResponse r = new PaymentResponse();
        r.setId(PAYMENT_ID);
        r.setSenderWalletId(SENDER_ID);
        r.setReceiverWalletId(UUID.fromString("00000000-0000-0000-0000-000000000020"));
        r.setAmount(new BigDecimal("5000"));
        r.setFeeAmount(BigDecimal.ZERO);
        r.setNetAmount(new BigDecimal("5000"));
        r.setCurrency("XOF");
        r.setType(PaymentType.P2P);
        r.setStatus(PaymentStatus.INITIATED);
        r.setPaymentReference("PAY-20260717-12345");
        r.setIdempotencyKey("KEY-001");
        return r;
    }
}
