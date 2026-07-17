package net.tanguydev.paymentservice.Domain.Ports;

import net.tanguydev.paymentservice.Domain.Commands.WalletCommand;

public interface WalletCommandPublisherInterface {
    void publish(WalletCommand command);
}
