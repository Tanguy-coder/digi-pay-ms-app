package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Enums.AccountStatus;
import net.tanguydev.customerservice.Domain.Enums.KycStatus;
import net.tanguydev.customerservice.Domain.Enums.TierLevel;
import net.tanguydev.customerservice.Domain.Events.CustomerEvent;
import net.tanguydev.customerservice.Domain.Ports.CustomerEventPublisherInterface;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.Validations.Exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {

    @Mock
    private CustomerServiceInterface customerService;

    @Mock
    private CustomerEventPublisherInterface eventPublisher;

    private CreateCustomerUseCase useCase;

    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        useCase = new CreateCustomerUseCase(customerService, eventPublisher);
    }

    @Test
    void execute_withValidCustomer_shouldSaveAndPublishEvent() {
        DomainCustomer customer = buildValidCustomer();
        customer.setId(CUSTOMER_ID);
        when(customerService.save(any(DomainCustomer.class))).thenReturn(customer);

        DomainCustomer result = useCase.execute(customer);

        assertNotNull(result);
        assertEquals("Tanguy", result.getFirstName());
        verify(customerService, times(1)).save(customer);

        ArgumentCaptor<CustomerEvent> eventCaptor = ArgumentCaptor.forClass(CustomerEvent.class);
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());

        CustomerEvent published = eventCaptor.getValue();
        assertEquals("customer.created", published.getEventType());
        assertEquals(CUSTOMER_ID, published.getCustomerId());
        assertEquals("Tanguy", published.getFirstName());
        assertEquals("XOF", published.getPreferredCurrency());
    }

    @Test
    void execute_withNullCustomer_shouldThrowValidationException() {
        assertThrows(DomainValidationException.class, () -> useCase.execute(null));
        verify(customerService, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_withMissingEmail_shouldThrowValidationException() {
        DomainCustomer customer = buildValidCustomer();
        customer.setEmail(null);

        assertThrows(DomainValidationException.class, () -> useCase.execute(customer));
        verify(customerService, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_withInvalidPhoneNumber_shouldThrowValidationException() {
        DomainCustomer customer = buildValidCustomer();
        customer.setPhoneNumber("12345");

        assertThrows(DomainValidationException.class, () -> useCase.execute(customer));
        verify(customerService, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_shouldSetDefaults_whenFieldsAreNull() {
        DomainCustomer customer = buildValidCustomer();
        customer.setStatus(null);
        customer.setKycStatus(null);
        customer.setTierLevel(null);
        customer.setRiskScore(null);
        customer.setIsEmailVerified(null);
        customer.setIsPhoneVerified(null);
        customer.setId(CUSTOMER_ID);

        when(customerService.save(any(DomainCustomer.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainCustomer result = useCase.execute(customer);

        assertEquals(AccountStatus.PENDING, result.getStatus());
        assertEquals(KycStatus.NOT_SUBMITTED, result.getKycStatus());
        assertEquals(TierLevel.BASIC, result.getTierLevel());
        assertEquals(BigDecimal.ZERO, result.getRiskScore());
        assertFalse(result.getIsEmailVerified());
        assertFalse(result.getIsPhoneVerified());
    }

    private DomainCustomer buildValidCustomer() {
        DomainCustomer customer = new DomainCustomer();
        customer.setFirstName("Tanguy");
        customer.setLastName("Mambafei");
        customer.setEmail("tanguy@example.com");
        customer.setPhoneNumber("+22890000000");
        customer.setNationality("TGO");
        customer.setAddressLine1("123 Rue de Lome");
        customer.setCity("Lome");
        customer.setCountry("Togo");
        customer.setStatus(AccountStatus.PENDING);
        customer.setKycStatus(KycStatus.NOT_SUBMITTED);
        customer.setTierLevel(TierLevel.BASIC);
        customer.setRiskScore(BigDecimal.ZERO);
        customer.setPreferredCurrency("XOF");
        return customer;
    }
}
