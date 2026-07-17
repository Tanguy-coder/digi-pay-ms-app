package net.tanguydev.paymentservice.Infrastructure.Controllers;

import net.tanguydev.paymentservice.Domain.Validations.Exception.DomainValidationException;
import net.tanguydev.paymentservice.Domain.Validations.Exception.DuplicatePaymentException;
import net.tanguydev.paymentservice.Domain.Validations.Exception.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("errors", fieldErrors));
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<Map<String, Object>> handleDomainValidation(DomainValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("errors", ex.getErrors()));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicatePaymentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
