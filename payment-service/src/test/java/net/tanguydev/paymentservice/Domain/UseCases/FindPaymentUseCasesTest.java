package net.tanguydev.paymentservice.Domain.UseCases;

import net.tanguydev.paymentservice.Domain.Entities.DomainPayment;
import net.tanguydev.paymentservice.Domain.Enums.PaymentStatus;
import net.tanguydev.paymentservice.Domain.Enums.PaymentType;
import net.tanguydev.paymentservice.Domain.Ports.PaymentServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindPaymentUseCasesTest {

    @Mock private PaymentServiceInterface paymentService;

    private FindPaymentByIdUseCase findById;
    private FindPaymentsByWalletUseCase findByWallet;

    private static final UUID PAYMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WALLET_ID  = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID OTHER_ID   = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @BeforeEach
    void setUp() {
        findById    = new FindPaymentByIdUseCase(paymentService);
        findByWallet = new FindPaymentsByWalletUseCase(paymentService);
    }

    @Test
    void findById_shouldReturnPayment_whenExists() {
        DomainPayment payment = buildPayment();
        when(paymentService.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        Optional<DomainPayment> result = findById.execute(PAYMENT_ID);

        assertTrue(result.isPresent());
        assertEquals(PAYMENT_ID, result.get().getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(paymentService.findById(OTHER_ID)).thenReturn(Optional.empty());

        Optional<DomainPayment> result = findById.execute(OTHER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void findByWallet_shouldAggregateSentAndReceived() {
        DomainPayment sent     = buildPayment();
        DomainPayment received = buildPayment();
        received.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));

        when(paymentService.findBySenderWalletId(WALLET_ID)).thenReturn(List.of(sent));
        when(paymentService.findByReceiverWalletId(WALLET_ID)).thenReturn(List.of(received));

        List<DomainPayment> result = findByWallet.execute(WALLET_ID);

        assertEquals(2, result.size());
    }

    @Test
    void findByWallet_shouldReturnEmpty_whenNoPayments() {
        when(paymentService.findBySenderWalletId(OTHER_ID)).thenReturn(List.of());
        when(paymentService.findByReceiverWalletId(OTHER_ID)).thenReturn(List.of());

        List<DomainPayment> result = findByWallet.execute(OTHER_ID);

        assertTrue(result.isEmpty());
    }

    private DomainPayment buildPayment() {
        DomainPayment p = new DomainPayment();
        p.setId(PAYMENT_ID);
        p.setSenderWalletId(WALLET_ID);
        p.setReceiverWalletId(UUID.fromString("00000000-0000-0000-0000-000000000020"));
        p.setAmount(new BigDecimal("5000"));
        p.setFeeAmount(BigDecimal.ZERO);
        p.setCurrency("XOF");
        p.setType(PaymentType.P2P);
        p.setStatus(PaymentStatus.COMPLETED);
        return p;
    }
}
