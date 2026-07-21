package net.tanguydev.settlementservice.Infrastructure.Controllers;

import net.tanguydev.settlementservice.Domain.Validations.Exception.BatchNotFoundException;
import net.tanguydev.settlementservice.Domain.Validations.Exception.DuplicateEntryException;
import net.tanguydev.settlementservice.Domain.Validations.Exception.InvalidBatchStatusException;
import net.tanguydev.settlementservice.Domain.Validations.Exception.NoOpenBatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BatchNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBatchNotFound(BatchNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoOpenBatchException.class)
    public ResponseEntity<Map<String, String>> handleNoOpenBatch(NoOpenBatchException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidBatchStatusException.class)
    public ResponseEntity<Map<String, String>> handleInvalidBatchStatus(InvalidBatchStatusException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEntry(DuplicateEntryException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
