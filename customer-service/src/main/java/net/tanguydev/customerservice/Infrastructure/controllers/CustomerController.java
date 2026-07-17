package net.tanguydev.customerservice.Infrastructure.controllers;

import jakarta.validation.Valid;
import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Presenters.CustomerPresenterInterface;
import net.tanguydev.customerservice.Domain.Responses.CustomerResponse;
import net.tanguydev.customerservice.Domain.UseCases.CreateCustomerUseCaseInterface;
import net.tanguydev.customerservice.Domain.UseCases.FindCutomerByIdUseCaseInterface;
import net.tanguydev.customerservice.Domain.UseCases.ListCustomersUseCaseInterface;
import net.tanguydev.customerservice.Domain.UseCases.UpdateCustomerUseCaseInterface;
import net.tanguydev.customerservice.Infrastructure.Mappers.CustomerMapper;
import net.tanguydev.customerservice.Infrastructure.Requests.CustomerRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CreateCustomerUseCaseInterface create;
    private final FindCutomerByIdUseCaseInterface find;
    private final ListCustomersUseCaseInterface list;
    private final UpdateCustomerUseCaseInterface update;
    private final CustomerPresenterInterface presenter;
    private final CustomerMapper mapper;

    public CustomerController(
            CreateCustomerUseCaseInterface create,
            FindCutomerByIdUseCaseInterface find,
            ListCustomersUseCaseInterface list,
            UpdateCustomerUseCaseInterface update,
            CustomerPresenterInterface presenter,
            CustomerMapper mapper)
    {
        this.create = create;
        this.find = find;
        this.list = list;
        this.update = update;
        this.presenter = presenter;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> index() {
        return ResponseEntity.ok(presenter.present(list.execute()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> show(@PathVariable UUID id) {
        return find.execute(id)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> store(@Valid @RequestBody CustomerRequest request) {
        DomainCustomer domainCustomer = mapper.requestToDomain(request);
        DomainCustomer created = create.execute(domainCustomer);
        return ResponseEntity.status(201).body(presenter.present(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        DomainCustomer domainCustomer = mapper.requestToDomain(request);
        DomainCustomer updated = update.execute(id, domainCustomer);
        return ResponseEntity.ok(presenter.present(updated));
    }
}
