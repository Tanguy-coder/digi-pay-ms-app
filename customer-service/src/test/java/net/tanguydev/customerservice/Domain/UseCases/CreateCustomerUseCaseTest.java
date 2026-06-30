package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Enums.AccountStatus;
import net.tanguydev.customerservice.Domain.Enums.KycStatus;
import net.tanguydev.customerservice.Domain.Enums.TierLevel;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.Validations.Exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {

    @Mock
    private CustomerServiceInterface customerService;

    private CreateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCustomerUseCase(customerService);
    }

    @Test
    void execute_withValidCustomer_shouldSaveAndReturn() {
        DomainCustomer customer = buildValidCustomer();
        when(customerService.save(any(DomainCustomer.class))).thenReturn(customer);

        DomainCustomer result = useCase.execute(customer);

        assertNotNull(result);
        assertEquals("Tanguy", result.getFirstName());
        verify(customerService, times(1)).save(customer);
    }

    @Test
    void execute_withNullCustomer_shouldThrowValidationException() {
        assertThrows(DomainValidationException.class, () -> useCase.execute(null));
        verify(customerService, never()).save(any());
    }

    @Test
    void execute_withMissingEmail_shouldThrowValidationException() {
        DomainCustomer customer = buildValidCustomer();
        customer.setEmail(null);

        assertThrows(DomainValidationException.class, () -> useCase.execute(customer));
        verify(customerService, never()).save(any());
    }

    @Test
    void execute_withInvalidPhoneNumber_shouldThrowValidationException() {
        DomainCustomer customer = buildValidCustomer();
        customer.setPhoneNumber("12345");

        assertThrows(DomainValidationException.class, () -> useCase.execute(customer));
        verify(customerService, never()).save(any());
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
