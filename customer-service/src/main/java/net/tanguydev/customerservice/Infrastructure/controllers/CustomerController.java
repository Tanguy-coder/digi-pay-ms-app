package net.tanguydev.customerservice.Infrastructure.controllers;

import net.tanguydev.customerservice.Domain.Presenters.CustomerPresenterInterface;
import net.tanguydev.customerservice.Domain.Responses.CustomerResponse;
import net.tanguydev.customerservice.Domain.UseCases.CreateCustomerUseCaseInterface;
import net.tanguydev.customerservice.Domain.UseCases.FindCutomerByIdUseCaseInterface;
import net.tanguydev.customerservice.Domain.UseCases.ListCustomersUseCaseInterface;
import net.tanguydev.customerservice.Infrastructure.Mappers.CustomerMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CreateCustomerUseCaseInterface create;
    private final FindCutomerByIdUseCaseInterface find;
    private final ListCustomersUseCaseInterface list;
    private final CustomerPresenterInterface presenter;
    private final CustomerMapper mapper;

    public CustomerController(
            CreateCustomerUseCaseInterface create,
            FindCutomerByIdUseCaseInterface find,
            ListCustomersUseCaseInterface list,
            CustomerPresenterInterface presenter,
            CustomerMapper mapper)
    {
        this.create = create;
        this.find = find;
        this.list = list;
        this.presenter = presenter;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> index() {
        return ResponseEntity.ok(presenter.present(list.execute()));
    }

}
