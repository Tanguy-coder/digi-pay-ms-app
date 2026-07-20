package net.tanguydev.settlementservice.Infrastructure.Controllers;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
import net.tanguydev.settlementservice.Domain.Ports.NetPositionRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementBatchRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Presenters.BatchPresenterInterface;
import net.tanguydev.settlementservice.Domain.Responses.BatchResponse;
import net.tanguydev.settlementservice.Domain.UseCases.CloseBatchUseCaseInterface;
import net.tanguydev.settlementservice.Domain.UseCases.OpenBatchUseCaseInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SettlementBatchRepositoryInterface batchRepository;

    @MockitoBean
    private SettlementEntryRepositoryInterface entryRepository;

    @MockitoBean
    private NetPositionRepositoryInterface positionRepository;

    @MockitoBean
    private BatchPresenterInterface presenter;

    @MockitoBean
    private OpenBatchUseCaseInterface openBatchUseCase;

    @MockitoBean
    private CloseBatchUseCaseInterface closeBatchUseCase;

    private DomainSettlementBatch domainBatch(UUID id) {
        DomainSettlementBatch batch = new DomainSettlementBatch();
        batch.setId(id);
        batch.setReference("BATCH-XAF-20260719-100000");
        batch.setStatus(BatchStatus.COLLECTING);
        batch.setCycle(SettlementCycle.HOURLY);
        batch.setCurrency("XAF");
        batch.setTotalEntries(3);
        batch.setTotalAmount(new BigDecimal("15000"));
        batch.setOpenedAt(OffsetDateTime.now());
        return batch;
    }

    private BatchResponse batchResponse(UUID id) {
        BatchResponse response = new BatchResponse();
        response.setId(id);
        response.setReference("BATCH-XAF-20260719-100000");
        response.setStatus("COLLECTING");
        response.setCycle("HOURLY");
        response.setCurrency("XAF");
        response.setTotalEntries(3);
        response.setTotalAmount(new BigDecimal("15000"));
        return response;
    }

    @Test
    void getAllBatches_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        DomainSettlementBatch batch = domainBatch(id);
        BatchResponse response = batchResponse(id);

        when(batchRepository.findAll()).thenReturn(List.of(batch));
        when(presenter.present(batch)).thenReturn(response);

        mockMvc.perform(get("/api/settlements/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference").value("BATCH-XAF-20260719-100000"))
                .andExpect(jsonPath("$[0].status").value("COLLECTING"));
    }

    @Test
    void getAllBatches_empty() throws Exception {
        when(batchRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/settlements/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getBatchById_found() throws Exception {
        UUID id = UUID.randomUUID();
        DomainSettlementBatch batch = domainBatch(id);
        BatchResponse response = batchResponse(id);

        when(batchRepository.findById(id)).thenReturn(Optional.of(batch));
        when(entryRepository.findByBatchId(id)).thenReturn(Collections.emptyList());
        when(positionRepository.findByBatchId(id)).thenReturn(Collections.emptyList());
        when(presenter.present(any(DomainSettlementBatch.class))).thenReturn(response);

        mockMvc.perform(get("/api/settlements/batches/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getBatchById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(batchRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/settlements/batches/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void openBatch_returnsCreated() throws Exception {
        UUID id = UUID.randomUUID();
        DomainSettlementBatch batch = domainBatch(id);
        BatchResponse response = batchResponse(id);

        when(openBatchUseCase.execute(SettlementCycle.HOURLY, "XAF")).thenReturn(batch);
        when(presenter.present(batch)).thenReturn(response);

        mockMvc.perform(post("/api/settlements/batches/open")
                        .param("cycle", "HOURLY")
                        .param("currency", "XAF"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("BATCH-XAF-20260719-100000"));
    }
}
