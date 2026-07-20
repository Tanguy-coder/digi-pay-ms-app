package net.tanguydev.customerservice.Infrastructure.controllers;

import jakarta.validation.Valid;
import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Presenters.CustomerPresenterInterface;
import net.tanguydev.customerservice.Domain.Responses.CustomerResponse;
import net.tanguydev.customerservice.Domain.UseCases.CreateCustomerUseCaseInterface;
import net.tanguydev.customerservice.Domain.UseCases.UpdateCustomerUseCaseInterface;
import net.tanguydev.customerservice.Infrastructure.Mappers.CustomerMapper;
import net.tanguydev.customerservice.Infrastructure.Requests.CustomerRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerCommandController {

    private final CreateCustomerUseCaseInterface create;
    private final UpdateCustomerUseCaseInterface update;
    private final CustomerPresenterInterface presenter;
    private final CustomerMapper mapper;

    public CustomerCommandController(CreateCustomerUseCaseInterface create,
                                     UpdateCustomerUseCaseInterface update,
                                     CustomerPresenterInterface presenter,
                                     CustomerMapper mapper) {
        this.create = create;
        this.update = update;
        this.presenter = presenter;
        this.mapper = mapper;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CustomerResponse> store(@Valid @RequestBody CustomerRequest request) {
        DomainCustomer domainCustomer = mapper.requestToDomain(request);
        DomainCustomer created = create.execute(domainCustomer);
        return ResponseEntity.status(201).body(presenter.present(created));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        DomainCustomer domainCustomer = mapper.requestToDomain(request);
        DomainCustomer updated = update.execute(id, domainCustomer);
        return ResponseEntity.ok(presenter.present(updated));
    }
}
