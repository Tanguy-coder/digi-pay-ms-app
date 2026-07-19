package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletEventType;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Ports.EventStoreInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.Validations.Exception.InsufficientBalanceException;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotActiveException;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FreezeAmountUseCaseTest {

    @Mock private WalletServiceInterface walletService;
    @Mock private WalletEventPublisherInterface eventPublisher;
    @Mock private EventStoreInterface eventStore;

    private FreezeAmountUseCase useCase;

    private static final UUID WALLET_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @BeforeEach
    void setUp() {
        useCase = new FreezeAmountUseCase(walletService, eventPublisher, eventStore);
    }

    @Test
    void execute_shouldFreezeAndPublishEvent() {
        when(eventStore.loadEvents(WALLET_ID)).thenReturn(buildWalletWithBalance(new BigDecimal("10000")));
        when(walletService.save(any(DomainWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainWallet result = useCase.execute(WALLET_ID, new BigDecimal("4000"));

        assertEquals(new BigDecimal("4000"), result.getFrozenAmount());
        assertEquals(new BigDecimal("10000"), result.getBalance());

        ArgumentCaptor<WalletEventEntry> storeCaptor = ArgumentCaptor.forClass(WalletEventEntry.class);
        verify(eventStore).append(storeCaptor.capture());
        assertEquals(WalletEventType.AMOUNT_FROZEN, storeCaptor.getValue().getEventType());

        ArgumentCaptor<WalletEvent> kafkaCaptor = ArgumentCaptor.forClass(WalletEvent.class);
        verify(eventPublisher).publish(kafkaCaptor.capture());
        assertEquals("wallet.amount_frozen", kafkaCaptor.getValue().getEventType());
        assertEquals(new BigDecimal("4000"), kafkaCaptor.getValue().getFrozenAmountAfter());
    }

    @Test
    void execute_shouldThrow_whenInsufficientAvailableBalance() {
        List<WalletEventEntry> events = buildWalletWithBalance(new BigDecimal("10000"));
        WalletEventEntry freeze = new WalletEventEntry();
        freeze.setWalletId(WALLET_ID);
        freeze.setEventType(WalletEventType.AMOUNT_FROZEN);
        freeze.setAmount(new BigDecimal("8000"));
        freeze.setAggregateVersion(3L);
        freeze.setOccurredAt(OffsetDateTime.now());

        List<WalletEventEntry> allEvents = new java.util.ArrayList<>(events);
        allEvents.add(freeze);

        when(eventStore.loadEvents(WALLET_ID)).thenReturn(allEvents);

        assertThrows(InsufficientBalanceException.class,
                () -> useCase.execute(WALLET_ID, new BigDecimal("5000")));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_shouldThrow_whenWalletNotFound() {
        when(eventStore.loadEvents(WALLET_ID)).thenReturn(Collections.emptyList());

        assertThrows(WalletNotFoundException.class, () -> useCase.execute(WALLET_ID, BigDecimal.TEN));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_shouldThrow_whenWalletNotActive() {
        WalletEventEntry created = new WalletEventEntry();
        created.setWalletId(WALLET_ID);
        created.setEventType(WalletEventType.WALLET_CREATED);
        created.setCustomerId(CUSTOMER_ID);
        created.setWalletNumber("WLT-001");
        created.setWalletType(WalletType.PERSONAL);
        created.setCurrency("XOF");
        created.setStatus(WalletStatus.FROZEN);
        created.setAggregateVersion(1L);
        created.setOccurredAt(OffsetDateTime.now());

        when(eventStore.loadEvents(WALLET_ID)).thenReturn(List.of(created));

        assertThrows(WalletNotActiveException.class, () -> useCase.execute(WALLET_ID, BigDecimal.TEN));
        verify(eventPublisher, never()).publish(any());
    }

    private List<WalletEventEntry> buildWalletWithBalance(BigDecimal balance) {
        WalletEventEntry created = new WalletEventEntry();
        created.setWalletId(WALLET_ID);
        created.setEventType(WalletEventType.WALLET_CREATED);
        created.setCustomerId(CUSTOMER_ID);
        created.setWalletNumber("WLT-001");
        created.setWalletType(WalletType.PERSONAL);
        created.setCurrency("XOF");
        created.setStatus(WalletStatus.ACTIVE);
        created.setAggregateVersion(1L);
        created.setOccurredAt(OffsetDateTime.now());

        WalletEventEntry credit = new WalletEventEntry();
        credit.setWalletId(WALLET_ID);
        credit.setEventType(WalletEventType.WALLET_CREDITED);
        credit.setAmount(balance);
        credit.setAggregateVersion(2L);
        credit.setOccurredAt(OffsetDateTime.now());

        return List.of(created, credit);
    }
}
