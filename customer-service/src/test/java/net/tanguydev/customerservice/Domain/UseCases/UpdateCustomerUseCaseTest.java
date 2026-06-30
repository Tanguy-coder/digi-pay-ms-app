package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Enums.AccountStatus;
import net.tanguydev.customerservice.Domain.Enums.KycStatus;
import net.tanguydev.customerservice.Domain.Enums.TierLevel;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import net.tanguydev.customerservice.Domain.Validations.Exception.CustomerNotFoundException;
import net.tanguydev.customerservice.Domain.Validations.Exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerUseCaseTest {

    @Mock
    private CustomerServiceInterface customerService;

    private UpdateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCustomerUseCase(customerService);
    }

    @Test
    void execute_withExistingCustomer_shouldUpdateAndReturn() {
        DomainCustomer existing = buildExistingCustomer();
        DomainCustomer updatedData = buildUpdatedData();

        when(customerService.findById(1L)).thenReturn(Optional.of(existing));
        when(customerService.save(any(DomainCustomer.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainCustomer result = useCase.execute(1L, updatedData);

        assertEquals("Kofi", result.getFirstName());
        assertEquals("Anan", result.getLastName());
        assertEquals("kofi@example.com", result.getEmail());
        assertNotNull(result.getUpdatedAt());
        // Les champs metier ne changent pas
        assertEquals(AccountStatus.PENDING, result.getStatus());
        assertEquals(KycStatus.NOT_SUBMITTED, result.getKycStatus());
        assertEquals(TierLevel.BASIC, result.getTierLevel());
        verify(customerService, times(1)).save(any());
    }

    @Test
    void execute_withNonExistingCustomer_shouldThrowNotFoundException() {
        when(customerService.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> useCase.execute(99L, buildUpdatedData()));
        verify(customerService, never()).save(any());
    }

    @Test
    void execute_withInvalidData_shouldThrowValidationException() {
        DomainCustomer existing = buildExistingCustomer();
        DomainCustomer updatedData = buildUpdatedData();
        updatedData.setEmail("invalid-email");

        when(customerService.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(DomainValidationException.class, () -> useCase.execute(1L, updatedData));
        verify(customerService, never()).save(any());
    }

    private DomainCustomer buildExistingCustomer() {
        DomainCustomer customer = new DomainCustomer();
        customer.setId(1L);
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
        customer.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return customer;
    }

    private DomainCustomer buildUpdatedData() {
        DomainCustomer customer = new DomainCustomer();
        customer.setFirstName("Kofi");
        customer.setLastName("Anan");
        customer.setEmail("kofi@example.com");
        customer.setPhoneNumber("+22891111111");
        customer.setNationality("GHA");
        customer.setAddressLine1("456 Accra Street");
        customer.setCity("Accra");
        customer.setCountry("Ghana");
        customer.setPreferredCurrency("GHS");
        return customer;
    }
}
