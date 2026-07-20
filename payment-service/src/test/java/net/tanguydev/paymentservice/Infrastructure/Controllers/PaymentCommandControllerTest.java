package net.tanguydev.paymentservice.Infrastructure.Controllers;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Enums.PaymentType;
import net.tanguydev.paymentservice.Domain.Presenters.PaymentPresenterInterface;
import net.tanguydev.paymentservice.Domain.Responses.PaymentResponse;
import net.tanguydev.paymentservice.Domain.UseCases.InitiatePaymentUseCaseInterface;
import net.tanguydev.paymentservice.Domain.Validations.Exception.DuplicatePaymentException;
import net.tanguydev.paymentservice.Infrastructure.Mappers.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentCommandController.class)
class PaymentCommandControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private InitiatePaymentUseCaseInterface initiate;
    @MockitoBean private PaymentPresenterInterface presenter;
    @MockitoBean private PaymentMapper mapper;

    private static final UUID PAYMENT_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SENDER_ID   = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID RECEIVER_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");

    private static final String VALID_REQUEST = """
            {
              "senderWalletId": "%s",
              "receiverWalletId": "%s",
              "amount": 5000,
              "currency": "XOF",
              "type": "P2P",
              "idempotencyKey": "KEY-001"
            }
            """.formatted(SENDER_ID, RECEIVER_ID);

    @Test
    void store_shouldReturn201() throws Exception {
        DomainPayment domain = buildDomain();
        PaymentResponse response = buildResponse();

        when(mapper.requestToDomain(any())).thenReturn(domain);
        when(initiate.execute(any(DomainPayment.class))).thenReturn(domain);
        when(presenter.present(any(DomainPayment.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("XOF"))
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }

    @Test
    void store_shouldReturn422_whenMandatoryFieldMissing() throws Exception {
        String missingCurrency = """
                {
                  "senderWalletId": "%s",
                  "receiverWalletId": "%s",
                  "amount": 5000,
                  "type": "P2P",
                  "idempotencyKey": "KEY-001"
                }
                """.formatted(SENDER_ID, RECEIVER_ID);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingCurrency))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void store_shouldReturn409_whenDuplicateKey() throws Exception {
        when(mapper.requestToDomain(any())).thenReturn(buildDomain());
        when(initiate.execute(any())).thenThrow(new DuplicatePaymentException("KEY-001"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isConflict());
    }

    private DomainPayment buildDomain() {
        DomainPayment p = new DomainPayment();
        p.setId(PAYMENT_ID);
        p.setSenderWalletId(SENDER_ID);
        p.setReceiverWalletId(RECEIVER_ID);
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
        r.setReceiverWalletId(RECEIVER_ID);
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
