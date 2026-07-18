package net.tanguydev.fraudservice.Domain.Ports;

import java.util.UUID;

public interface VelocityCounterInterface {

    int countLastMinute(UUID senderWalletId);

    int countLastHour(UUID senderWalletId);
}
