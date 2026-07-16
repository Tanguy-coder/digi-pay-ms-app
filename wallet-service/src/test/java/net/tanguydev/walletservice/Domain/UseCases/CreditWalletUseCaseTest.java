package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
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
class CreditWalletUseCaseTest {

    @Mock
    private WalletServiceInterface walletService;

    @Mock
    private WalletEventPublisherInterface eventPublisher;

    private CreditWalletUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreditWalletUseCase(walletService, eventPublisher);
    }

    @Test
    void execute_shouldCreditAndPublishEvent() {
        DomainWallet wallet = buildActiveWallet();
        when(walletService.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletService.save(any(DomainWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainWallet result = useCase.execute(1L, new BigDecimal("5000"));

        assertEquals(new BigDecimal("15000"), result.getBalance());

        ArgumentCaptor<WalletEvent> captor = ArgumentCaptor.forClass(WalletEvent.class);
        verify(eventPublisher).publish(captor.capture());

        WalletEvent event = captor.getValue();
        assertEquals("wallet.credited", event.getEventType());
        assertEquals(1L, event.getWalletId());
        assertEquals(new BigDecimal("5000"), event.getAmount());
        assertEquals(new BigDecimal("15000"), event.getBalanceAfter());
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
        wallet.setStatus(WalletStatus.FROZEN);
        when(walletService.findById(1L)).thenReturn(Optional.of(wallet));

        assertThrows(WalletNotActiveException.class, () -> useCase.execute(1L, BigDecimal.TEN));
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
