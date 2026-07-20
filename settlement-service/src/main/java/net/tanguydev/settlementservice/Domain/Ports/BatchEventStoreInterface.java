package net.tanguydev.settlementservice.Domain.Ports;

import net.tanguydev.settlementservice.Domain.Events.BatchEventEntry;

import java.util.List;
import java.util.UUID;

public interface BatchEventStoreInterface {

    void append(BatchEventEntry event);

    List<BatchEventEntry> loadEvents(UUID batchId);

    List<BatchEventEntry> loadEventsSince(UUID batchId, Long afterVersion);
}
