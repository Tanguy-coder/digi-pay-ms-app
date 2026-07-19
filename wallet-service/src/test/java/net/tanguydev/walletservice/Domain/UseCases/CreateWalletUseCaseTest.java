package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Ports.EventStoreInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseTest {

    @Mock private WalletServiceInterface walletService;
    @Mock private WalletEventPublisherInterface eventPublisher;
    @Mock private EventStoreInterface eventStore;

    private CreateWalletUseCase useCase;

    private static final UUID WALLET_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @BeforeEach
    void setUp() {
        useCase = new CreateWalletUseCase(walletService, eventPublisher, eventStore);
    }

    @Test
    void execute_shouldPersistEventAndPublishToKafka() {
        DomainWallet wallet = new DomainWallet();
        wallet.setCustomerId(CUSTOMER_ID);
        wallet.setCurrency("XOF");

        when(walletService.save(any(DomainWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainWallet result = useCase.execute(wallet);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(CUSTOMER_ID, result.getCustomerId());
        assertEquals(WalletStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getBalance());

        ArgumentCaptor<WalletEventEntry> entryCaptor = ArgumentCaptor.forClass(WalletEventEntry.class);
        verify(eventStore).append(entryCaptor.capture());
        assertEquals(net.tanguydev.walletservice.Domain.Enums.WalletEventType.WALLET_CREATED,
                entryCaptor.getValue().getEventType());

        ArgumentCaptor<WalletEvent> kafkaCaptor = ArgumentCaptor.forClass(WalletEvent.class);
        verify(eventPublisher).publish(kafkaCaptor.capture());
        assertEquals("wallet.created", kafkaCaptor.getValue().getEventType());
    }

    @Test
    void execute_shouldSetDefaults_whenFieldsNull() {
        DomainWallet wallet = new DomainWallet();
        wallet.setCustomerId(CUSTOMER_ID);
        wallet.setCurrency("XOF");

        when(walletService.save(any(DomainWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainWallet result = useCase.execute(wallet);

        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(BigDecimal.ZERO, result.getFrozenAmount());
        assertEquals(WalletStatus.ACTIVE, result.getStatus());
        assertEquals(WalletType.PERSONAL, result.getWalletType());
        assertNotNull(result.getWalletNumber());
        assertTrue(result.getWalletNumber().startsWith("WLT-"));
    }
}
