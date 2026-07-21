package net.tanguydev.notificationservice.Infrastructure.Controllers;

import net.tanguydev.notificationservice.Domain.Entities.DomainNotification;
import net.tanguydev.notificationservice.Domain.Enums.NotificationStatus;
import net.tanguydev.notificationservice.Domain.Enums.NotificationType;
import net.tanguydev.notificationservice.Domain.Ports.NotificationRepositoryInterface;
import net.tanguydev.notificationservice.Domain.Presenters.NotificationPresenterInterface;
import net.tanguydev.notificationservice.Domain.Responses.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationQueryController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationRepositoryInterface repository;

    @MockitoBean
    private NotificationPresenterInterface presenter;

    private NotificationResponse dto(UUID walletId, UUID paymentId, NotificationType type) {
        NotificationResponse r = new NotificationResponse();
        r.setId(UUID.randomUUID());
        r.setWalletId(walletId);
        r.setPaymentId(paymentId);
        r.setType(type);
        r.setStatus(NotificationStatus.SENT);
        r.setMessage("Payment of 100.00 EUR completed successfully.");
        r.setAmount(new BigDecimal("100.00"));
        r.setCurrency("EUR");
        r.setCreatedAt(OffsetDateTime.now());
        return r;
    }

    // ── GET /api/v1/notifications/wallet/{walletId} ────────────────────────────

    @Test
    void showByWallet_returnsListOf200() throws Exception {
        UUID walletId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        DomainNotification domain = new DomainNotification();
        NotificationResponse dto = dto(walletId, paymentId, NotificationType.PAYMENT_COMPLETED);

        when(repository.findByWalletId(walletId)).thenReturn(List.of(domain));
        when(presenter.present(List.of(domain))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/notifications/wallet/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].walletId").value(walletId.toString()))
                .andExpect(jsonPath("$[0].type").value("PAYMENT_COMPLETED"));
    }

    @Test
    void showByWallet_noHistory_returnsEmptyList() throws Exception {
        UUID walletId = UUID.randomUUID();

        when(repository.findByWalletId(walletId)).thenReturn(List.of());
        when(presenter.present(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications/wallet/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void showByWallet_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/wallet/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/v1/notifications/payment/{paymentId} ─────────────────────────

    @Test
    void showByPayment_returnsListOf200() throws Exception {
        UUID walletId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        DomainNotification domain = new DomainNotification();
        NotificationResponse dto = dto(walletId, paymentId, NotificationType.PAYMENT_INITIATED);

        when(repository.findByPaymentId(paymentId)).thenReturn(List.of(domain));
        when(presenter.present(List.of(domain))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/notifications/payment/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$[0].type").value("PAYMENT_INITIATED"));
    }

    @Test
    void showByPayment_noHistory_returnsEmptyList() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(repository.findByPaymentId(paymentId)).thenReturn(List.of());
        when(presenter.present(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications/payment/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
