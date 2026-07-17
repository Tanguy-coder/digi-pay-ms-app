package net.tanguydev.walletservice.Domain.Ports;

import net.tanguydev.walletservice.Domain.Events.WalletEvent;

public interface WalletEventPublisherInterface {

    void publish(WalletEvent event);
}
