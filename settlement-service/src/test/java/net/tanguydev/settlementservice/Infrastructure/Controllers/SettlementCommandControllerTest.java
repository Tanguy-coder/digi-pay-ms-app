package net.tanguydev.settlementservice.Infrastructure.Controllers;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementBatch;
import net.tanguydev.settlementservice.Domain.Enums.BatchStatus;
import net.tanguydev.settlementservice.Domain.Enums.SettlementCycle;
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
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettlementCommandController.class)
class SettlementCommandControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private OpenBatchUseCaseInterface openBatchUseCase;
    @MockitoBean private CloseBatchUseCaseInterface closeBatchUseCase;
    @MockitoBean private BatchPresenterInterface presenter;

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
}
