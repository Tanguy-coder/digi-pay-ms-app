package net.tanguydev.customerservice.Infrastructure.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.tanguydev.customerservice.Domain.Presenters.CustomerPresenterInterface;
import net.tanguydev.customerservice.Domain.Responses.CustomerResponse;
import net.tanguydev.customerservice.Domain.UseCases.FindCutomerByIdUseCaseInterface;
import net.tanguydev.customerservice.Domain.UseCases.ListCustomersUseCaseInterface;
import net.tanguydev.customerservice.Domain.Validations.Exception.CustomerNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Customers")
@RestController
@RequestMapping("/api/v1/customers")
@SecurityRequirement(name = "BearerAuth")
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

    @Operation(summary = "List all customers",
            responses = @ApiResponse(responseCode = "200", description = "Customer list"))
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> index() {
        return ResponseEntity.ok(presenter.present(list.execute()));
    }

    @Operation(summary = "Get a customer by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer found"),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> show(@PathVariable UUID id) {
        return find.execute(id)
                .map(presenter::present)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }
}
