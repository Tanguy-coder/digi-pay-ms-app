package net.tanguydev.customerservice.Domain.Ports;

import net.tanguydev.customerservice.Domain.Events.CustomerEvent;

public interface CustomerEventPublisherInterface {
    void publish(CustomerEvent event);
}
