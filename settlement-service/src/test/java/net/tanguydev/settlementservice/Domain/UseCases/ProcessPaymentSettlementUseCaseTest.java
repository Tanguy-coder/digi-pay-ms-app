package net.tanguydev.settlementservice.Domain.UseCases;

import net.tanguydev.settlementservice.Domain.Entities.DomainSettlement;
import net.tanguydev.settlementservice.Domain.Entities.DomainSettlementEntry;
import net.tanguydev.settlementservice.Domain.Enums.SettlementStatus;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEntryRepositoryInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementEventPublisherInterface;
import net.tanguydev.settlementservice.Domain.Ports.SettlementRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentSettlementUseCaseTest {

    @Mock private SettlementRepositoryInterface settlementRepository;
    @Mock private SettlementEntryRepositoryInterface entryRepository;
    @Mock private SettlementEventPublisherInterface eventPublisher;

    private ProcessPaymentSettlementUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPaymentSettlementUseCase(settlementRepository, entryRepository, eventPublisher);
    }

    private ProcessPaymentSettlementCommand command() {
        ProcessPaymentSettlementCommand cmd = new ProcessPaymentSettlementCommand();
        cmd.setPaymentId(UUID.randomUUID());
        cmd.setPaymentReference("PAY-REF-001");
        cmd.setSenderWalletId(UUID.randomUUID());
        cmd.setReceiverWalletId(UUID.randomUUID());
        cmd.setAmount(new BigDecimal("5000.00"));
        cmd.setFeeAmount(new BigDecimal("50.00"));
        cmd.setCurrency("XAF");
        return cmd;
    }

    @Test
    void execute_createsSettlementWithCorrectFields() {
        ProcessPaymentSettlementCommand cmd = command();
        when(entryRepository.existsByPaymentId(cmd.getPaymentId())).thenReturn(false);
        when(settlementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(cmd);

        ArgumentCaptor<DomainSettlement> captor = ArgumentCaptor.forClass(DomainSettlement.class);
        verify(settlementRepository, times(2)).save(captor.capture());

        DomainSettlement saved = captor.getAllValues().get(1);
        assertThat(saved.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
        assertThat(saved.getReference()).isEqualTo("SET-PAY-REF-001");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("5000.00");
        assertThat(saved.getCurrency()).isEqualTo("XAF");
        assertThat(saved.getSettledAt()).isNotNull();
    }

    @Test
    void execute_createsTwoEntries_debitAndCredit() {
        ProcessPaymentSettlementCommand cmd = command();
        when(entryRepository.existsByPaymentId(cmd.getPaymentId())).thenReturn(false);
        when(settlementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(cmd);

        ArgumentCaptor<DomainSettlementEntry> captor = ArgumentCaptor.forClass(DomainSettlementEntry.class);
        verify(entryRepository, times(2)).save(captor.capture());

        DomainSettlementEntry debit = captor.getAllValues().get(0);
        DomainSettlementEntry credit = captor.getAllValues().get(1);

        assertThat(debit.getEntryType().name()).isEqualTo("DEBIT");
        assertThat(debit.getWalletId()).isEqualTo(cmd.getSenderWalletId());
        assertThat(credit.getEntryType().name()).isEqualTo("CREDIT");
        assertThat(credit.getWalletId()).isEqualTo(cmd.getReceiverWalletId());
    }

    @Test
    void execute_publishesSettlementCompletedEvent() {
        ProcessPaymentSettlementCommand cmd = command();
        when(entryRepository.existsByPaymentId(cmd.getPaymentId())).thenReturn(false);
        when(settlementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(cmd);

        verify(eventPublisher).publishSettlementCompleted(any(DomainSettlement.class));
    }

    @Test
    void execute_duplicatePayment_skipsProcessing() {
        ProcessPaymentSettlementCommand cmd = command();
        when(entryRepository.existsByPaymentId(cmd.getPaymentId())).thenReturn(true);

        useCase.execute(cmd);

        verify(settlementRepository, never()).save(any());
        verify(entryRepository, never()).save(any());
        verify(eventPublisher, never()).publishSettlementCompleted(any());
    }

    @Test
    void execute_netPositionIsZero() {
        ProcessPaymentSettlementCommand cmd = command();
        when(entryRepository.existsByPaymentId(cmd.getPaymentId())).thenReturn(false);
        when(settlementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(cmd);

        ArgumentCaptor<DomainSettlement> captor = ArgumentCaptor.forClass(DomainSettlement.class);
        verify(settlementRepository, times(2)).save(captor.capture());

        DomainSettlement completed = captor.getAllValues().get(1);
        assertThat(completed.getNetPosition()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
