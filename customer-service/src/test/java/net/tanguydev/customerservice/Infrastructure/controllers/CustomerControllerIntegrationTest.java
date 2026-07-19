package net.tanguydev.customerservice.Infrastructure.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tanguydev.customerservice.Infrastructure.Models.Customer;
import net.tanguydev.customerservice.Infrastructure.Repositories.CustomerJpaRepository;
import net.tanguydev.customerservice.Infrastructure.Requests.CustomerRequest;
import net.tanguydev.customerservice.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @BeforeEach
    void setUp() {
        customerJpaRepository.deleteAll();
    }

    @Test
    void index_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void store_withValidRequest_shouldCreateCustomer() throws Exception {
        CustomerRequest request = buildValidRequest();

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("Tanguy")))
                .andExpect(jsonPath("$.lastName", is("Mambafei")))
                .andExpect(jsonPath("$.email", is("tanguy@example.com")))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void store_withInvalidRequest_shouldReturn422() throws Exception {
        CustomerRequest request = buildValidRequest();
        request.setEmail("invalid");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.email").isNotEmpty());
    }

    @Test
    void show_withExistingId_shouldReturnCustomer() throws Exception {
        Customer saved = customerJpaRepository.save(buildValidEntity());

        mockMvc.perform(get("/api/v1/customers/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Tanguy")))
                .andExpect(jsonPath("$.email", is("tanguy@example.com")));
    }

    @Test
    void show_withNonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/customers/00000000-0000-0000-0000-000000000999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_withValidRequest_shouldUpdateCustomer() throws Exception {
        Customer saved = customerJpaRepository.save(buildValidEntity());
        CustomerRequest request = buildValidRequest();
        request.setFirstName("Kofi");
        request.setLastName("Anan");

        mockMvc.perform(put("/api/v1/customers/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Kofi")))
                .andExpect(jsonPath("$.lastName", is("Anan")));
    }

    @Test
    void update_withNonExistingId_shouldReturn404() throws Exception {
        CustomerRequest request = buildValidRequest();

        mockMvc.perform(put("/api/v1/customers/00000000-0000-0000-0000-000000000999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    private CustomerRequest buildValidRequest() {
        return CustomerRequest.builder()
                .firstName("Tanguy")
                .lastName("Mambafei")
                .email("tanguy@example.com")
                .phoneNumber("+22890000000")
                .nationality("TGO")
                .addressLine1("123 Rue de Lome")
                .city("Lome")
                .country("Togo")
                .preferredCurrency("XOF")
                .build();
    }

    private Customer buildValidEntity() {
        return Customer.builder()
                .firstName("Tanguy")
                .lastName("Mambafei")
                .email("tanguy@example.com")
                .phoneNumber("+22890000000")
                .nationality("TGO")
                .addressLine1("123 Rue de Lome")
                .city("Lome")
                .country("Togo")
                .preferredCurrency("XOF")
                .build();
    }
}
