package net.tanguydev.customerservice.Infrastructure.controllers;

import net.tanguydev.customerservice.Domain.Presenters.CustomerPresenterInterface;
import net.tanguydev.customerservice.Domain.Responses.CustomerResponse;
import net.tanguydev.customerservice.Domain.UseCases.FindCutomerByIdUseCaseInterface;
import net.tanguydev.customerservice.Domain.UseCases.ListCustomersUseCaseInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerQueryController {

    private final FindCutomerByIdUseCaseInterface find;
    private final ListCustomersUseCaseInterface list;
    private final CustomerPresenterInterface presenter;

    public CustomerQueryController(FindCutomerByIdUseCaseInterface find,
                                   ListCustomersUseCaseInterface list,
                                   CustomerPresenterInterface presenter) {
        this.find = find;
        this.list = list;
        this.presenter = presenter;
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
}
