package net.tanguydev.settlementservice.Infrastructure.Controllers;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Enums.SettlementStatus;
import net.tanguydev.settlementservice.Domain.Ports.SettlementRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Presenters.SettlementPresenterInterface;
import net.tanguydev.settlementservice.Domain.Responses.SettlementResponse;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SettlementRepositoryInterface settlementRepository;

    @MockitoBean
    private SettlementPresenterInterface presenter;

    private DomainSettlement domainSettlement(UUID id) {
        DomainSettlement s = new DomainSettlement();
        s.setId(id);
        s.setReference("SET-PAY-001");
        s.setStatus(SettlementStatus.COMPLETED);
        s.setCycle(SettlementCycle.MANUAL);
        s.setCurrency("XAF");
        s.setTotalPayments(1);
        s.setTotalAmount(new BigDecimal("5000.00"));
        s.setNetPosition(BigDecimal.ZERO);
        s.setPeriodStart(OffsetDateTime.now());
        s.setPeriodEnd(OffsetDateTime.now());
        s.setSettledAt(OffsetDateTime.now());
        s.setCreatedAt(OffsetDateTime.now());
        return s;
    }

    private SettlementResponse response(UUID id) {
        SettlementResponse r = new SettlementResponse();
        r.setId(id);
        r.setReference("SET-PAY-001");
        r.setStatus(SettlementStatus.COMPLETED);
        r.setCycle(SettlementCycle.MANUAL);
        r.setCurrency("XAF");
        r.setTotalPayments(1);
        r.setTotalAmount(new BigDecimal("5000.00"));
        r.setNetPosition(BigDecimal.ZERO);
        r.setSettledAt(OffsetDateTime.now());
        r.setCreatedAt(OffsetDateTime.now());
        return r;
    }

    @Test
    void getAll_returnsListOf200() throws Exception {
        UUID id = UUID.randomUUID();
        DomainSettlement domain = domainSettlement(id);
        SettlementResponse dto = response(id);

        when(settlementRepository.findAll()).thenReturn(List.of(domain));
        when(presenter.present(domain)).thenReturn(dto);

        mockMvc.perform(get("/api/settlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference").value("SET-PAY-001"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].currency").value("XAF"));
    }

    @Test
    void getAll_empty_returnsEmptyList() throws Exception {
        when(settlementRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/settlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_found_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        DomainSettlement domain = domainSettlement(id);
        SettlementResponse dto = response(id);

        when(settlementRepository.findById(id)).thenReturn(Optional.of(domain));
        when(presenter.present(domain)).thenReturn(dto);

        mockMvc.perform(get("/api/settlements/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.reference").value("SET-PAY-001"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(settlementRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/settlements/{id}", id))
                .andExpect(status().isNotFound());
    }
}
