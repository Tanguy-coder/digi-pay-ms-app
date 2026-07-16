package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseTest {

    @Mock
    private WalletServiceInterface walletService;

    @Mock
    private WalletEventPublisherInterface eventPublisher;

    private CreateWalletUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateWalletUseCase(walletService, eventPublisher);
    }

    @Test
    void execute_shouldCreateWalletAndPublishEvent() {
        DomainWallet wallet = new DomainWallet();
        wallet.setCustomerId(100L);
        wallet.setCurrency("XOF");

        DomainWallet saved = new DomainWallet();
        saved.setId(1L);
        saved.setCustomerId(100L);
        saved.setCurrency("XOF");
        saved.setBalance(BigDecimal.ZERO);
        saved.setFrozenAmount(BigDecimal.ZERO);
        saved.setStatus(WalletStatus.ACTIVE);

        when(walletService.save(any(DomainWallet.class))).thenReturn(saved);

        DomainWallet result = useCase.execute(wallet);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(WalletStatus.ACTIVE, result.getStatus());

        ArgumentCaptor<WalletEvent> captor = ArgumentCaptor.forClass(WalletEvent.class);
        verify(eventPublisher).publish(captor.capture());

        WalletEvent event = captor.getValue();
        assertEquals("wallet.created", event.getEventType());
        assertEquals(1L, event.getWalletId());
        assertEquals(100L, event.getCustomerId());
        assertEquals("XOF", event.getCurrency());
    }

    @Test
    void execute_shouldSetDefaults_whenFieldsNull() {
        DomainWallet wallet = new DomainWallet();
        wallet.setCustomerId(100L);
        wallet.setCurrency("XOF");

        when(walletService.save(any(DomainWallet.class))).thenAnswer(inv -> {
            DomainWallet w = inv.getArgument(0);
            w.setId(1L);
            return w;
        });

        DomainWallet result = useCase.execute(wallet);

        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(BigDecimal.ZERO, result.getFrozenAmount());
        assertEquals(WalletStatus.ACTIVE, result.getStatus());
    }
}
