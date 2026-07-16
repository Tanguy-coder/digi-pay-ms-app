package net.tanguydev.walletservice.Infrastructure.Consumers;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Component
public class CustomerEventConsumer {

    private final WalletServiceInterface walletService;
    private final WalletEventPublisherInterface eventPublisher;

    public CustomerEventConsumer(WalletServiceInterface walletService,
                                 WalletEventPublisherInterface eventPublisher) {
        this.walletService = walletService;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "customer-events", groupId = "wallet-group")
    public void consume(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");

        if ("customer.created".equals(eventType)) {
            handleCustomerCreated(message);
        }
    }

    private void handleCustomerCreated(Map<String, Object> message) {
        Long customerId = ((Number) message.get("customerId")).longValue();
        String currency = (String) message.get("preferredCurrency");

        if (walletService.findByCustomerId(customerId).isPresent()) {
            return;
        }

        DomainWallet wallet = new DomainWallet();
        wallet.setCustomerId(customerId);
        wallet.setCurrency(currency != null ? currency : "XOF");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setFrozenAmount(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);

        DomainWallet saved = walletService.save(wallet);

        WalletEvent event = new WalletEvent();
        event.setEventType("wallet.created");
        event.setWalletId(saved.getId());
        event.setCustomerId(saved.getCustomerId());
        event.setCurrency(saved.getCurrency());
        event.setAmount(null);
        event.setBalanceAfter(saved.getBalance());
        event.setFrozenAmountAfter(saved.getFrozenAmount());
        event.setStatus(saved.getStatus());
        event.setOccurredAt(OffsetDateTime.now());

        eventPublisher.publish(event);
    }
}
