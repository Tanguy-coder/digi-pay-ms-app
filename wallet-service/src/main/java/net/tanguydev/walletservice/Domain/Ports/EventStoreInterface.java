package net.tanguydev.walletservice.Domain.Ports;

import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;

import java.util.List;
import java.util.UUID;

public interface EventStoreInterface {

    void append(WalletEventEntry event);

    List<WalletEventEntry> loadEvents(UUID walletId);

    List<WalletEventEntry> loadEventsSince(UUID walletId, Long afterVersion);
}
