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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebitWalletUseCaseTest {

    @Mock
    private WalletServiceInterface walletService;

    @Mock
    private WalletEventPublisherInterface eventPublisher;

    private DebitWalletUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DebitWalletUseCase(walletService, eventPublisher);
    }

    @Test
    void execute_shouldDebitAndPublishEvent() {
        DomainWallet wallet = buildActiveWallet();
        when(walletService.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletService.save(any(DomainWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainWallet result = useCase.execute(1L, new BigDecimal("3000"));

        assertEquals(new BigDecimal("7000"), result.getBalance());

        ArgumentCaptor<WalletEvent> captor = ArgumentCaptor.forClass(WalletEvent.class);
        verify(eventPublisher).publish(captor.capture());

        WalletEvent event = captor.getValue();
        assertEquals("wallet.debited", event.getEventType());
        assertEquals(new BigDecimal("3000"), event.getAmount());
        assertEquals(new BigDecimal("7000"), event.getBalanceAfter());
    }

    @Test
    void execute_shouldThrow_whenInsufficientBalance() {
        DomainWallet wallet = buildActiveWallet();
        when(walletService.findById(1L)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class,
                () -> useCase.execute(1L, new BigDecimal("20000")));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_shouldThrow_whenWalletNotFound() {
        when(walletService.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> useCase.execute(99L, BigDecimal.TEN));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_shouldThrow_whenWalletNotActive() {
        DomainWallet wallet = buildActiveWallet();
        wallet.setStatus(WalletStatus.CLOSED);
        when(walletService.findById(1L)).thenReturn(Optional.of(wallet));

        assertThrows(WalletNotActiveException.class, () -> useCase.execute(1L, BigDecimal.TEN));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_shouldConsiderFrozenAmount() {
        DomainWallet wallet = buildActiveWallet();
        wallet.setFrozenAmount(new BigDecimal("8000"));
        when(walletService.findById(1L)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class,
                () -> useCase.execute(1L, new BigDecimal("5000")));
        verify(eventPublisher, never()).publish(any());
    }

    private DomainWallet buildActiveWallet() {
        DomainWallet w = new DomainWallet();
        w.setId(1L);
        w.setCustomerId(100L);
        w.setCurrency("XOF");
        w.setBalance(new BigDecimal("10000"));
        w.setFrozenAmount(BigDecimal.ZERO);
        w.setStatus(WalletStatus.ACTIVE);
        return w;
    }
}
