package net.tanguydev.customerservice.Domain.UseCases;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindCustomerByIdUseCaseTest {

    @Mock
    private CustomerServiceInterface customerService;

    private FindCustomerByIdUseCase useCase;

    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_ID    = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @BeforeEach
    void setUp() {
        useCase = new FindCustomerByIdUseCase(customerService);
    }

    @Test
    void execute_withExistingId_shouldReturnCustomer() {
        DomainCustomer customer = new DomainCustomer();
        customer.setId(CUSTOMER_ID);
        customer.setFirstName("Tanguy");

        when(customerService.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        Optional<DomainCustomer> result = useCase.execute(CUSTOMER_ID);

        assertTrue(result.isPresent());
        assertEquals("Tanguy", result.get().getFirstName());
        verify(customerService, times(1)).findById(CUSTOMER_ID);
    }

    @Test
    void execute_withNonExistingId_shouldReturnEmpty() {
        when(customerService.findById(OTHER_ID)).thenReturn(Optional.empty());

        Optional<DomainCustomer> result = useCase.execute(OTHER_ID);

        assertTrue(result.isEmpty());
        verify(customerService, times(1)).findById(OTHER_ID);
    }
}
