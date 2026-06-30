package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindCustomerByIdUseCaseTest {

    @Mock
    private CustomerServiceInterface customerService;

    private FindCustomerByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindCustomerByIdUseCase(customerService);
    }

    @Test
    void execute_withExistingId_shouldReturnCustomer() {
        DomainCustomer customer = new DomainCustomer();
        customer.setId(1L);
        customer.setFirstName("Tanguy");

        when(customerService.findById(1L)).thenReturn(Optional.of(customer));

        Optional<DomainCustomer> result = useCase.execute(1L);

        assertTrue(result.isPresent());
        assertEquals("Tanguy", result.get().getFirstName());
        verify(customerService, times(1)).findById(1L);
    }

    @Test
    void execute_withNonExistingId_shouldReturnEmpty() {
        when(customerService.findById(99L)).thenReturn(Optional.empty());

        Optional<DomainCustomer> result = useCase.execute(99L);

        assertTrue(result.isEmpty());
        verify(customerService, times(1)).findById(99L);
    }
}
