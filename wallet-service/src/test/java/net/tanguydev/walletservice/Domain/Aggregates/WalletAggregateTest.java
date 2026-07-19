package net.tanguydev.walletservice.Domain.Aggregates;

import net.tanguydev.walletservice.Domain.Enums.WalletEventType;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Validations.Exception.InsufficientBalanceException;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotActiveException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletAggregateTest {

    private static final UUID WALLET_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Test
    void createWallet_shouldInitializeState() {
        WalletAggregate aggregate = new WalletAggregate();
        aggregate.createWallet(WALLET_ID, CUSTOMER_ID, "WLT-001",
                WalletType.PERSONAL, "XOF", null, null);

        assertEquals(WALLET_ID, aggregate.getWalletId());
        assertEquals(CUSTOMER_ID, aggregate.getCustomerId());
        assertEquals(WalletStatus.ACTIVE, aggregate.getStatus());
        assertEquals(BigDecimal.ZERO, aggregate.getBalance());
        assertEquals(BigDecimal.ZERO, aggregate.getFrozenAmount());
        assertEquals(1, aggregate.getVersion());
        assertEquals(1, aggregate.getUncommittedEvents().size());
    }

    @Test
    void credit_shouldIncreaseBalance() {
        WalletAggregate aggregate = createActiveAggregate();
        aggregate.credit(new BigDecimal("5000"));

        assertEquals(new BigDecimal("5000"), aggregate.getBalance());
        assertEquals(1, aggregate.getUncommittedEvents().size());
    }

    @Test
    void debit_shouldDecreaseBalance() {
        WalletAggregate aggregate = createAggregateWithBalance(new BigDecimal("10000"));
        aggregate.debit(new BigDecimal("3000"));

        assertEquals(new BigDecimal("7000"), aggregate.getBalance());
    }

    @Test
    void debit_shouldThrow_whenInsufficientBalance() {
        WalletAggregate aggregate = createAggregateWithBalance(new BigDecimal("5000"));

        assertThrows(InsufficientBalanceException.class,
                () -> aggregate.debit(new BigDecimal("8000")));
    }

    @Test
    void freezeAmount_shouldIncreaseFrozenAmount() {
        WalletAggregate aggregate = createAggregateWithBalance(new BigDecimal("10000"));
        aggregate.freezeAmount(new BigDecimal("4000"));

        assertEquals(new BigDecimal("4000"), aggregate.getFrozenAmount());
        assertEquals(new BigDecimal("6000"), aggregate.getAvailableBalance());
    }

    @Test
    void debit_shouldConsiderFrozenAmount() {
        WalletAggregate aggregate = createAggregateWithBalance(new BigDecimal("10000"));
        aggregate.freezeAmount(new BigDecimal("8000"));

        assertThrows(InsufficientBalanceException.class,
                () -> aggregate.debit(new BigDecimal("5000")));
    }

    @Test
    void operations_shouldThrow_whenWalletNotActive() {
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

        WalletAggregate aggregate = WalletAggregate.reconstitute(List.of(created));

        assertThrows(WalletNotActiveException.class, () -> aggregate.credit(BigDecimal.TEN));
        assertThrows(WalletNotActiveException.class, () -> aggregate.debit(BigDecimal.TEN));
        assertThrows(WalletNotActiveException.class, () -> aggregate.freezeAmount(BigDecimal.TEN));
    }

    @Test
    void reconstitute_shouldReplayAllEvents() {
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

        WalletEventEntry credit1 = new WalletEventEntry();
        credit1.setWalletId(WALLET_ID);
        credit1.setEventType(WalletEventType.WALLET_CREDITED);
        credit1.setAmount(new BigDecimal("10000"));
        credit1.setAggregateVersion(2L);
        credit1.setOccurredAt(OffsetDateTime.now());

        WalletEventEntry debit1 = new WalletEventEntry();
        debit1.setWalletId(WALLET_ID);
        debit1.setEventType(WalletEventType.WALLET_DEBITED);
        debit1.setAmount(new BigDecimal("3000"));
        debit1.setAggregateVersion(3L);
        debit1.setOccurredAt(OffsetDateTime.now());

        WalletEventEntry freeze1 = new WalletEventEntry();
        freeze1.setWalletId(WALLET_ID);
        freeze1.setEventType(WalletEventType.AMOUNT_FROZEN);
        freeze1.setAmount(new BigDecimal("2000"));
        freeze1.setAggregateVersion(4L);
        freeze1.setOccurredAt(OffsetDateTime.now());

        WalletAggregate aggregate = WalletAggregate.reconstitute(List.of(created, credit1, debit1, freeze1));

        assertEquals(new BigDecimal("7000"), aggregate.getBalance());
        assertEquals(new BigDecimal("2000"), aggregate.getFrozenAmount());
        assertEquals(new BigDecimal("5000"), aggregate.getAvailableBalance());
        assertEquals(4, aggregate.getVersion());
        assertTrue(aggregate.getUncommittedEvents().isEmpty());
    }

    private WalletAggregate createActiveAggregate() {
        WalletAggregate aggregate = new WalletAggregate();
        aggregate.createWallet(WALLET_ID, CUSTOMER_ID, "WLT-001",
                WalletType.PERSONAL, "XOF", null, null);
        aggregate.markEventsCommitted();
        return aggregate;
    }

    private WalletAggregate createAggregateWithBalance(BigDecimal balance) {
        WalletAggregate aggregate = createActiveAggregate();
        aggregate.credit(balance);
        aggregate.markEventsCommitted();
        return aggregate;
    }
}
