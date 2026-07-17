package net.tanguydev.walletservice.Infrastructure.Consumers;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Events.WalletEvent;
import net.tanguydev.walletservice.Domain.Ports.WalletEventPublisherInterface;
import net.tanguydev.walletservice.Domain.Ports.WalletServiceInterface;
import net.tanguydev.walletservice.Domain.UseCases.CreateWalletUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

@Component
public class CustomerEventConsumer {

    private final WalletServiceInterface walletService;
    private final CreateWalletUseCase createWalletUseCase;

    public CustomerEventConsumer(WalletServiceInterface walletService,
                                 CreateWalletUseCase createWalletUseCase) {
        this.walletService = walletService;
        this.createWalletUseCase = createWalletUseCase;
    }

    @KafkaListener(topics = "customer-events", groupId = "wallet-group")
    public void consume(Map<String, Object> message) {
        String eventType = (String) message.get("eventType");

        if ("customer.created".equals(eventType)) {
            handleCustomerCreated(message);
        }
    }

    private void handleCustomerCreated(Map<String, Object> message) {
        UUID customerId = toUUID(message.get("customerId"));
        String currency = (String) message.get("preferredCurrency");

        if (walletService.findByCustomerId(customerId).isPresent()) {
            return;
        }

        DomainWallet wallet = new DomainWallet();
        wallet.setCustomerId(customerId);
        wallet.setWalletType(WalletType.PERSONAL);
        wallet.setCurrency(currency != null ? currency : "XOF");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setFrozenAmount(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);

        createWalletUseCase.execute(wallet);
    }

    // customerId est un Long côté customer-service → convertit en UUID déterministe
    private UUID toUUID(Object value) {
        if (value instanceof String s) return UUID.fromString(s);
        long id = ((Number) Objects.requireNonNull(value)).longValue();
        return new UUID(0L, id);
    }
}
