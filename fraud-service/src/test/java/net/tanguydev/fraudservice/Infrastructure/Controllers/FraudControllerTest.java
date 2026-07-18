package net.tanguydev.fraudservice.Infrastructure.Controllers;

import net.tanguydev.fraudservice.Domain.Entities.DomainFraudAnalysis;
import net.tanguydev.fraudservice.Domain.Enums.FraudVerdict;
import net.tanguydev.fraudservice.Domain.Ports.FraudAnalysisRepositoryInterface;
import net.tanguydev.fraudservice.Domain.Presenters.FraudAnalysisPresenterInterface;
import net.tanguydev.fraudservice.Domain.Responses.FraudAnalysisResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FraudController.class)
class FraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FraudAnalysisRepositoryInterface repository;

    @MockitoBean
    private FraudAnalysisPresenterInterface presenter;

    // ── helpers ────────────────────────────────────────────────────────────────

    private FraudAnalysisResponse response(UUID paymentId, UUID customerId, FraudVerdict verdict) {
        FraudAnalysisResponse r = new FraudAnalysisResponse();
        r.setId(UUID.randomUUID());
        r.setPaymentId(paymentId);
        r.setCustomerId(customerId);
        r.setRiskScore(new BigDecimal("85.00"));
        r.setVerdict(verdict);
        r.setCreatedAt(OffsetDateTime.now());
        return r;
    }

    // ── GET /api/v1/fraud-analyses/{paymentId} ─────────────────────────────────

    @Test
    void showByPayment_found_returns200() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        DomainFraudAnalysis domain = new DomainFraudAnalysis();
        FraudAnalysisResponse dto = response(paymentId, customerId, FraudVerdict.BLOCKED);

        when(repository.findByPaymentId(paymentId)).thenReturn(Optional.of(domain));
        when(presenter.present(domain)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/fraud-analyses/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.verdict").value("BLOCKED"));
    }

    @Test
    void showByPayment_notFound_returns404() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(repository.findByPaymentId(paymentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/fraud-analyses/{paymentId}", paymentId))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/v1/fraud-analyses/customer/{customerId} ──────────────────────

    @Test
    void showByCustomer_returnsListOf200() throws Exception {
        UUID customerId = UUID.randomUUID();
        DomainFraudAnalysis domain = new DomainFraudAnalysis();
        FraudAnalysisResponse dto = response(UUID.randomUUID(), customerId, FraudVerdict.CLEARED);

        when(repository.findByCustomerId(customerId)).thenReturn(List.of(domain));
        when(presenter.present(List.of(domain))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/fraud-analyses/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(customerId.toString()))
                .andExpect(jsonPath("$[0].verdict").value("CLEARED"));
    }

    @Test
    void showByCustomer_noHistory_returnsEmptyList() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(repository.findByCustomerId(customerId)).thenReturn(List.of());
        when(presenter.present(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/fraud-analyses/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void showByPayment_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/fraud-analyses/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
