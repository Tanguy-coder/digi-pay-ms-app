package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Enums.WalletEventType;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Ports.EventStoreInterface;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWalletHistoryUseCaseTest {

    @Mock private EventStoreInterface eventStore;

    private GetWalletHistoryUseCase useCase;

    private static final UUID WALLET_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @BeforeEach
    void setUp() {
        useCase = new GetWalletHistoryUseCase(eventStore);
    }

    @Test
    void execute_shouldReturnAllEvents() {
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
        credit.setAmount(new BigDecimal("5000"));
        credit.setAggregateVersion(2L);
        credit.setOccurredAt(OffsetDateTime.now());

        when(eventStore.loadEvents(WALLET_ID)).thenReturn(List.of(created, credit));

        List<WalletEventEntry> result = useCase.execute(WALLET_ID);

        assertEquals(2, result.size());
        assertEquals(WalletEventType.WALLET_CREATED, result.get(0).getEventType());
        assertEquals(WalletEventType.WALLET_CREDITED, result.get(1).getEventType());
    }

    @Test
    void execute_shouldThrow_whenNoEvents() {
        when(eventStore.loadEvents(WALLET_ID)).thenReturn(Collections.emptyList());

        assertThrows(WalletNotFoundException.class, () -> useCase.execute(WALLET_ID));
    }
}
