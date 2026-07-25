package net.tanguydev.settlementservice.Infrastructure.Controllers;

import net.tanguydev.settlementservice.Domain.Validations.Exception.BatchNotFoundException;
import net.tanguydev.settlementservice.Domain.Validations.Exception.DuplicateEntryException;
import net.tanguydev.settlementservice.Domain.Validations.Exception.InvalidBatchStatusException;
import net.tanguydev.settlementservice.Domain.Validations.Exception.NoOpenBatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BatchNotFoundException.class)
    public ProblemDetail handleBatchNotFound(BatchNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Batch Not Found");
        problem.setType(URI.create("https://digipay.io/errors/batch-not-found"));
        return problem;
    }

    @ExceptionHandler(NoOpenBatchException.class)
    public ProblemDetail handleNoOpenBatch(NoOpenBatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("No Open Batch");
        problem.setType(URI.create("https://digipay.io/errors/no-open-batch"));
        return problem;
    }

    @ExceptionHandler(InvalidBatchStatusException.class)
    public ProblemDetail handleInvalidBatchStatus(InvalidBatchStatusException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid Batch Status");
        problem.setType(URI.create("https://digipay.io/errors/invalid-batch-status"));
        return problem;
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ProblemDetail handleDuplicateEntry(DuplicateEntryException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Duplicate Entry");
        problem.setType(URI.create("https://digipay.io/errors/duplicate-entry"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        problem.setTitle("Validation Error");
        problem.setType(URI.create("https://digipay.io/errors/validation-error"));
        problem.setProperty("errors", errors);
        return problem;
    }
}
