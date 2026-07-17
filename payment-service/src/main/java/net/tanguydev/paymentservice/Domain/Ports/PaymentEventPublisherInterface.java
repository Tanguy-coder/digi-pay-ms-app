package net.tanguydev.paymentservice.Domain.Ports;

import net.tanguydev.paymentservice.Domain.Events.PaymentEvent;

public interface PaymentEventPublisherInterface {
    void publish(PaymentEvent event);
}
