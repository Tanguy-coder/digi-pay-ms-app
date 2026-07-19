package net.tanguydev.walletservice.Domain.UseCases;

import net.tanguydev.walletservice.Domain.Events.WalletEventEntry;
import net.tanguydev.walletservice.Domain.Ports.EventStoreInterface;
import net.tanguydev.walletservice.Domain.Validations.Exception.WalletNotFoundException;

import java.util.List;
import java.util.UUID;

public class GetWalletHistoryUseCase implements GetWalletHistoryUseCaseInterface {

    private final EventStoreInterface eventStore;

    public GetWalletHistoryUseCase(EventStoreInterface eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public List<WalletEventEntry> execute(UUID walletId) {
        List<WalletEventEntry> events = eventStore.loadEvents(walletId);
        if (events.isEmpty()) {
            throw new WalletNotFoundException(walletId);
        }
        return events;
    }
}
