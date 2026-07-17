package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FreezeAmountUseCaseTest {

    @Mock
    private WalletServiceInterface walletService;

    @Mock
    private WalletEventPublisherInterface eventPublisher;

    private FreezeAmountUseCase useCase;

    private static final UUID WALLET_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_ID   = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @BeforeEach
    void setUp() {
        useCase = new FreezeAmountUseCase(walletService, eventPublisher);
    }

    @Test
    void execute_shouldFreezeAndPublishEvent() {
        DomainWallet wallet = buildActiveWallet();
        when(walletService.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        when(walletService.save(any(DomainWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainWallet result = useCase.execute(WALLET_ID, new BigDecimal("4000"));

        assertEquals(new BigDecimal("4000"), result.getFrozenAmount());
        assertEquals(new BigDecimal("10000"), result.getBalance());

        ArgumentCaptor<WalletEvent> captor = ArgumentCaptor.forClass(WalletEvent.class);
        verify(eventPublisher).publish(captor.capture());

        WalletEvent event = captor.getValue();
        assertEquals("wallet.amount_frozen", event.getEventType());
        assertEquals(new BigDecimal("4000"), event.getAmount());
        assertEquals(new BigDecimal("4000"), event.getFrozenAmountAfter());
    }

    @Test
    void execute_shouldThrow_whenInsufficientAvailableBalance() {
        DomainWallet wallet = buildActiveWallet();
        wallet.setFrozenAmount(new BigDecimal("8000"));
        when(walletService.findById(WALLET_ID)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class,
                () -> useCase.execute(WALLET_ID, new BigDecimal("5000")));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_shouldThrow_whenWalletNotFound() {
        when(walletService.findById(OTHER_ID)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> useCase.execute(OTHER_ID, BigDecimal.TEN));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_shouldThrow_whenWalletNotActive() {
        DomainWallet wallet = buildActiveWallet();
        wallet.setStatus(WalletStatus.FROZEN);
        when(walletService.findById(WALLET_ID)).thenReturn(Optional.of(wallet));

        assertThrows(WalletNotActiveException.class, () -> useCase.execute(WALLET_ID, BigDecimal.TEN));
        verify(eventPublisher, never()).publish(any());
    }

    private DomainWallet buildActiveWallet() {
        DomainWallet w = new DomainWallet();
        w.setId(WALLET_ID);
        w.setCustomerId(CUSTOMER_ID);
        w.setCurrency("XOF");
        w.setBalance(new BigDecimal("10000"));
        w.setFrozenAmount(BigDecimal.ZERO);
        w.setStatus(WalletStatus.ACTIVE);
        return w;
    }
}
